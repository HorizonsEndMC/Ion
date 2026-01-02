package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.ScramblerBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.ScramblerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ScramblerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.time.Duration

class ScramblerProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	private val subsystem: ScramblerWeaponSubsystem,
) : LaserProjectile<ScramblerBalancing.ScramblerProjectileBalancing>(source, name, loc, dir, shooter, ScramblerStarshipWeaponMultiblock.damageType) {

	// Lazily copied heavy lazer slow
	override val color: Color = Color.BLUE

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {

		val speedPenalty = 0.1

		starship.userErrorAction("Direct Control speed slowed by ${(speedPenalty * 100).toInt()}%!")
		// starship was not slowed by heavy lasers recently
		// Only start the timer based on the first hit
		starship.directControlSlowExpiryFromHeavyLasers =
			System.currentTimeMillis() + Duration.ofSeconds(20L).toMillis()
		//}
		// Reduce starship speed by the slow factor
		starship.directControlSpeedModifierFromHeavyLasers *= (1 - speedPenalty)

		Tasks.syncDelay(Duration.ofSeconds(20L).toSeconds() * 20L) {
			// reset for individual shots
			starship.directControlSpeedModifierFromHeavyLasers /= (1 - speedPenalty)
			if (ActiveStarships.isActive(starship) && starship.directControlSlowExpiryFromHeavyLasers - 100 < System.currentTimeMillis()) {
				// hard reset to normal speed (I feel that weird double-rounding bugs might be possible)
				starship.directControlSpeedModifierFromHeavyLasers = 1.0
				starship.directControlSlowExpiryFromHeavyLasers = 0L
				starship.informationAction("Direct Control speed restored")
			}
		}

		val shooterStarship = shooter.starship ?: return

		val task = Tasks.syncRepeatTask(0L, 2L) {
			val startLocation = subsystem.getFirePos().toLocation(location.world).toCenterLocation()

			for (endLocation in impactLocation.circlePoints(10.0, 30, direction)) {
				shooterStarship.world.spawnParticle(
					Particle.TRAIL,
					startLocation,
					1,
					0.5,
					0.5,
					0.5,
					0.0,
					Particle.Trail(endLocation, color, randomInt(15, 25)),
					true
				)
			}
		}

		Tasks.syncDelay(20L) {
			task.cancel()
		}

	}
}
