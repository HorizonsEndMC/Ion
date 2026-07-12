package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.ScramblerBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.ScramblerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ScramblerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

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
		val shooterStarship = shooter.starship ?: return

		val task = Tasks.syncRepeatTask(0L, 2L) {
			val startLocation = subsystem.getFirePos().toLocation(location.world).toCenterLocation()

			for (endLocation in impactLocation.circlePoints(10.0, 10, direction)) {
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

		if (starship.initialBlockCount > 12500) return

		val speedPenalty = 1 - balancing.effectStrength

		starship.addStatusEffect(StarshipStatusEffect(
			StarshipStatusEffectTypes.DIRECT_CONTROL_SLOW,
			speedPenalty,
			balancing.effectDurationMillis,
			shooter.starship,
		))

		starship.userErrorAction("Direct Control speed slowed by ${(speedPenalty * 100).toInt()}%!")


		starship.addStatusEffect(StarshipStatusEffect(
			StarshipStatusEffectTypes.JAMMED,
			1.0,
			balancing.effectDurationMillis,
			shooter.starship,
		))
	}
}
