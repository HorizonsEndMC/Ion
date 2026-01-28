package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lerp
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class SwarmMissileProjectile(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    val dir: Vector,
    val initialDir: Vector,
    target: Vector,
    val color: Color,
    shooter: Damager,
    otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : BoidProjectile<SwarmMissileBalancing.SwarmMissileProjectileBalancing>(source, name, loc, dir, target, shooter, otherBoids, damageType), ProximityProjectile {
    companion object {
        private const val ADDITIONAL_EXPLOSION_POWER = 2.0f
    }

    override val proximityRange: Double = balancing.proximityRange
    var flightPath1Completed = false
    var flightPath2Completed = false

    val item = ItemFactory.unStackableCustomItem("projectile/swarm_missile").construct { t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color)) }

    private val container = ItemDisplayContainer(
        source.getWorld(),
        2.0F,
        loc.toVector(),
        dir,
        item,
        interpolation = 2
    ).apply {
        getEntity().transformationInterpolationDuration = 2
        getEntity().teleportDuration = 2
    }

    override fun tick() {

        if (!flightPath1Completed) {
            // Initial launch - fly straight out of the multiblock
            direction = initialDir.clone().multiply(1) // make the projectile launch parallel from the launcher, and slower

            if (distance > 30) {
                distance = 0.0
                flightPath1Completed = true
            }

            super.tick()
            return
        } else if (!flightPath2Completed) {
            // Transitioning to free flight - linearly interpolate between initial launch and free flight
            direction = direction.clone().lerp(calculateBoidDirection(direction), 0.2)

            if (direction.angle(dir) < Math.PI / 6) {
                flightPath2Completed = true
            }

            super.tick()
            return
        }

        // Free flight
        direction = calculateBoidDirection(direction)

        val closestStarship = getStarshipsInProximity(location).minByOrNull { starship ->
            starship.centerOfMass.toVector().distanceSquared(location.toVector())
        }

        // Maneuver to nearby starships
        if (closestStarship != null && closestStarship.identifier != shooter.starship?.identifier) {
            val oldDirection = direction.clone()
            direction = oldDirection.lerp(closestStarship.centerOfMass.toVector().subtract(location.toVector()).normalize().multiply(oldDirection.length()),
                lerpAmount(closestStarship.centerOfMass.toVector().distance(location.toVector()))
            )
        }

        super.tick()
    }

    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        container.position = location.toVector()
        container.heading = direction.clone()
        container.update()

        /*for (lineLoc in oldLocation.alongVector(newLocation.toVector().subtract(oldLocation.toVector()), 5)) {
            lineLoc.world.spawnParticle(Particle.DUST, lineLoc.x, lineLoc.y, lineLoc.z, 1, 0.0, 0.0, 0.0, 0.0, Particle.DustOptions(color, 2f), true)
        }*/

        (0 until 2).forEach { _ ->
            val angle = Math.PI / 24
            val opposite = direction.clone().multiply(-1)
                .rotateAroundX(randomDouble(-angle, angle))
                .rotateAroundY(randomDouble(-angle, angle))
                .rotateAroundZ(randomDouble(-angle, angle))
            location.world.spawnParticle(Particle.LARGE_SMOKE, location, 0, opposite.x, opposite.y, opposite.z, 1.0, null, true)
            location.world.spawnParticle(Particle.FLAME, location, 0, opposite.x, opposite.y, opposite.z, 0.25, null, true)
        }
    }

    override fun onDespawn() {
        container.remove()
    }

    private fun lerpAmount(distance: Double): Double {
        val distanceRatio = distance / proximityRange
        return when {
            distanceRatio <= 0.0 -> 1.0 // projectile is basically inside the target
            distanceRatio >= 1.0 -> 0.0 // projectile is further than the proximity range
            else -> /*1.00262 * 0.0511657.pow(distance / proximityRange)*/ 1 - distanceRatio
        }
    }

    override fun onImpact() {
        for (loc in location.circlePoints(2.0, 30, direction)) {
            val radialVector = loc.toVector().subtract(location.toVector()).normalize()
            loc.world.spawnParticle(Particle.CLOUD, location, 0, radialVector.x, radialVector.y, radialVector.z, 1.0, null, true)
        }

        (0 until 20).forEach { _ ->
            val angle = Math.PI / 12
            val opposite = direction.clone().multiply(-1)
                .rotateAroundX(randomDouble(-angle, angle))
                .rotateAroundY(randomDouble(-angle, angle))
                .rotateAroundZ(randomDouble(-angle, angle))
            location.world.spawnParticle(Particle.FLAME, location, 0, opposite.x, opposite.y, opposite.z, 1.0, null, true)
        }

        (0 until 40).forEach { _ ->
            val angle = Math.PI / 6
            val opposite = direction.clone().multiply(-1)
                .rotateAroundX(randomDouble(-angle, angle))
                .rotateAroundY(randomDouble(-angle, angle))
                .rotateAroundZ(randomDouble(-angle, angle))
            location.world.spawnParticle(Particle.LARGE_SMOKE, location, 0, opposite.x, opposite.y, opposite.z, 2.0, null, true)
        }
    }

    /*
    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        if (starship.type != StarshipType.STARFIGHTER) {
            impactLocation.createExplosion(ADDITIONAL_EXPLOSION_POWER)

            // explosionOccurred only controls the hull hitmarker sound; just use this to increase damager points on the target
            addToDamagers(impactLocation.world, impactLocation.block, shooter, ADDITIONAL_EXPLOSION_POWER.roundToInt(), explosionOccurred = false, runStarshipImpactEvent = false
            )
        }
    }
     */
}