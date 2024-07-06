package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DisintegratorBeamWeaponSubsystem
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class DisintegratorBeamProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    override val range: Double,
    shooter: Damager,
    private val subsystem: DisintegratorBeamWeaponSubsystem,
    damage: Double
) : LaserProjectile(starship, loc, dir, shooter) {

    companion object {
        private const val RESET_STACK_TIME_MILLIS = 5000L
    }

    override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.disintegratorBeam ?: IonServer.starshipBalancing.nonStarshipFired.disintegratorBeam
    override val speed: Double = balancing.speed
    override val starshipShieldDamageMultiplier = damage
    override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
    override val color: Color = Color.ORANGE
    override val particleThickness: Double = balancing.particleThickness
    override val explosionPower: Float = damage.toFloat()
    override val volume: Int = balancing.volume
    override val pitch: Float = balancing.pitch
    override val soundName: String = balancing.soundName

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        super.onImpactStarship(starship, impactLocation)

        val currentTime = System.nanoTime()
        if (currentTime - subsystem.lastImpact > TimeUnit.MILLISECONDS.toNanos(RESET_STACK_TIME_MILLIS) ) {
            subsystem.beamStacks = 1
        } else {
            subsystem.beamStacks += 1
        }

        CrystalLaser(subsystem.getFirePos().toLocation(starship.world), impactLocation, 5, -1)
            .durationInTicks().apply { start(IonServer) }

        subsystem.lastImpact = currentTime
    }

    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        // Do not spawn particle
    }

    override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
        super.impact(newLoc, block, entity)

        newLoc.world.spawnParticle(
            Particle.LAVA,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            explosionPower.toInt() * 20,
            explosionPower.toDouble() / 2,
            explosionPower.toDouble() / 2,
            explosionPower.toDouble() / 2,
            0.0,
            null,
            true
        )

        newLoc.world.spawnParticle(
            Particle.SMOKE_LARGE,
            newLoc.x,
            newLoc.y,
            newLoc.z,
            explosionPower.toInt() * 10,
            explosionPower.toDouble(),
            explosionPower.toDouble(),
            explosionPower.toDouble(),
            0.0,
            null,
            true
        )
    }
}