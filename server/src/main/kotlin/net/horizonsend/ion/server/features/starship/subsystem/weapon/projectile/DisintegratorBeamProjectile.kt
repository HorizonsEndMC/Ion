package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.starship.DisintegratorBeamBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DisintegratorBeamWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class DisintegratorBeamProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	private val subsystem: DisintegratorBeamWeaponSubsystem,
	damage: Double
) : LaserProjectile<DisintegratorBeamBalancing.DisintegratorBeamProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

    companion object {
        private const val RESET_STACK_TIME_MILLIS = 4000L
    }

    override val starshipShieldDamageMultiplier = damage
    override val color: Color = Color.ORANGE
    override val explosionPower: Float = damage.toFloat()

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        super.onImpactStarship(starship, impactLocation)

        val currentTime = System.nanoTime()
        // notify weapon subsystem to increment stacks
        if (currentTime - subsystem.lastImpact > TimeUnit.MILLISECONDS.toNanos(RESET_STACK_TIME_MILLIS)) {
            subsystem.beamStacks = 1
        } else {
            subsystem.beamStacks += 1
        }

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
            Particle.LARGE_SMOKE,
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

        CrystalLaser(subsystem.getFirePos().toLocation(newLoc.world), newLoc, 5, -1)
            .durationInTicks().apply { start(IonServer) }

        val currentTime = System.nanoTime()
        // do not build stacks if hitting non-starship objects
        if (currentTime - subsystem.lastImpact > TimeUnit.MILLISECONDS.toNanos(RESET_STACK_TIME_MILLIS)) {
            subsystem.beamStacks = 1
        }
    }
}
