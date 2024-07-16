package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.EntityDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.iterateVector
import net.horizonsend.ion.server.miscellaneous.utils.spherePoints
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector

class DoomsdayDeviceProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    shooter: Damager
) : ParticleProjectile(starship, loc, dir, shooter) {
    override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.doomsdayDevice ?: IonServer.starshipBalancing.nonStarshipFired.doomsdayDevice
    override val range: Double = balancing.range
    override var speed: Double = balancing.speed
    override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
    override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
    override val explosionPower: Float = balancing.explosionPower
    override val volume: Int = balancing.volume
    override val pitch: Float = balancing.pitch
    override val soundName: String = balancing.soundName

    private val greenParticleData = Particle.DustTransition(
        Color.fromARGB(255, 182, 255, 0),
        Color.BLACK,
        balancing.particleThickness.toFloat()
    )

    private val yellowParticleData = Particle.DustTransition(
        Color.YELLOW,
        Color.BLACK,
        balancing.particleThickness.toFloat()
    )

    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {


        Location(loc.world, x, y, z).spherePoints(3.0, 20).forEach {
            it.world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION,
                it.x,
                it.y,
                it.z,
                1,
                0.5,
                0.5,
                0.5,
                2.0,
                greenParticleData,
                force
            )
        }

        Tasks.syncDelay(5) {
            Location(loc.world, x, y, z).spherePoints(1.5, 5).forEach {
                it.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    it.x,
                    it.y,
                    it.z,
                    1,
                    0.25,
                    0.25,
                    0.25,
                    2.0,
                    yellowParticleData,
                    force
                )
            }
        }
    }

    // overriding the entire tick() function just to change the raySize :weary:
    override fun tick() {
        delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds

        val predictedNewLoc = loc.clone().add(dir.clone().multiply(delta * speed))
        if (!predictedNewLoc.isChunkLoaded) {
            return
        }
        val result: RayTraceResult? = loc.world.rayTrace(loc, dir, delta * speed, FluidCollisionMode.NEVER, true, 0.5) { it.type != EntityType.ITEM_DISPLAY }
        val newLoc = result?.hitPosition?.toLocation(loc.world) ?: predictedNewLoc
        val travel = loc.distance(newLoc)

        moveVisually(loc, newLoc, travel)

        var impacted = false

        if (result != null) {
            impacted = tryImpact(result, newLoc)
        }

        loc = newLoc

        distance += travel

        if (impacted) {
            return
        }

        if (distance >= range) {
            return
        }

        lastTick = System.nanoTime()
        reschedule()

    }

    override fun onHitEntity(entity: LivingEntity) {
        when (shooter) {
            is PlayerDamager -> entity.damage(10000.0, shooter.player)
            is EntityDamager -> entity.damage(10000.0, shooter.entity)
            else -> entity.damage(10000.0)
        }
    }

    override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
        super.impact(newLoc, block, entity)

        newLoc.world.spawnParticle(
            Particle.LAVA,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            200,
            3.0,
            3.0,
            3.0,
            0.0,
            null,
            true
        )

        newLoc.world.spawnParticle(
            Particle.WHITE_ASH,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            250,
            5.0,
            5.0,
            5.0,
            0.0,
            null,
            true
        )

        newLoc.world.spawnParticle(
            Particle.ASH,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            200,
            5.0,
            5.0,
            5.0,
            0.0,
            null,
            true
        )

        for (point in newLoc.spherePoints(15.0, 20)) {
            newLoc.iterateVector(Vector(point.x - newLoc.x, point.y - newLoc.y, point.z - newLoc.z), 10) { pointAlong, _ ->
                pointAlong.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    pointAlong.x,
                    pointAlong.y,
                    pointAlong.z,
                    1,
                    0.5,
                    0.5,
                    0.5,
                    2.0,
                    greenParticleData,
                    true
                )
            }
        }

        for (point in newLoc.spherePoints(30.0, 10)) {
            newLoc.iterateVector(Vector(point.x - newLoc.x, point.y - newLoc.y, point.z - newLoc.z), 20) { pointAlong, _ ->
                pointAlong.world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    pointAlong.x,
                    pointAlong.y,
                    pointAlong.z,
                    1,
                    0.5,
                    0.5,
                    0.5,
                    2.0,
                    yellowParticleData,
                    true
                )
            }
        }
    }
}