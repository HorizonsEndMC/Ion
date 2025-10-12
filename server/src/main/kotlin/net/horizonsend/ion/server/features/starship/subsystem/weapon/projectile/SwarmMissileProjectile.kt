package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lerp
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.math.pow

class SwarmMissileProjectile(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    val dir: Vector,
    val initialDir: Vector,
    shooter: Damager,
    otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : BoidParticleProjectile<SwarmMissileBalancing.SwarmMissileProjectileBalancing>(source, name, loc, dir, shooter, otherBoids, damageType), ProximityProjectile {
    override val proximityRange: Double = balancing.proximityRange
    var flightPath1Completed = false
    var flightPath2Completed = false

    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
        val particle = Particle.DUST
        val dustOptions = Particle.DustOptions(Color.AQUA, 5f)

        location.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
    }

    override fun tick() {
        if (!flightPath1Completed) {
            // Initial launch - fly straight out of the multiblock
            direction = initialDir.clone().multiply(0.25) // make the projectile launch parallel from the launcher, and slower

            if (distance > 10) {
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

    private fun lerpAmount(distance: Double): Double {
        val distanceRatio = distance / proximityRange
        return when {
            distanceRatio <= 0.0 -> 1.0 // projectile is basically inside the target
            distanceRatio >= 1.0 -> 0.0 // projectile is further than the proximity range
            else -> 1.00262 * 0.0511657.pow(distance / proximityRange) /*1 - distanceRatio*/
        }
    }
}