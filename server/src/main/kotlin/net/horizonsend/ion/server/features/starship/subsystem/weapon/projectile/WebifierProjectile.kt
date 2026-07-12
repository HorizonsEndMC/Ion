package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.WebifierBalancing.WebifierProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.WebifierStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.WebifierWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.pow

class WebifierProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int,
	private val subsystem: WebifierWeaponSubsystem
) : TrackingLaserProjectile<WebifierProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, WebifierStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.TEAL

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		super.spawnParticle(x, y, z, force)
	}

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		val shooterStarship = shooter.starship ?: return

		val task = Tasks.syncRepeatTask(0L, 4L) {
			val endLocation = subsystem.getFirePos().toLocation(shooterStarship.world).toCenterLocation()

			for (startPoint in impactLocation.circlePoints(10.0, 30, direction)) {
				shooterStarship.world.spawnParticle(
					Particle.TRAIL,
					startPoint,
					1,
					0.5,
					0.5,
					0.5,
					0.0,
					Particle.Trail(endLocation, color, randomInt(20, 60)),
					true
				)
			}
		}
		Tasks.syncDelay(60L) {
			task.cancel()
		}

		if (starship.initialBlockCount < 12501) {

			val speedPenalty = 1 - balancing.effectStrength

			starship.addStatusEffect(
				StarshipStatusEffect(
					StarshipStatusEffectTypes.DIRECT_CONTROL_SLOW,
					speedPenalty,
					balancing.effectDurationMillis,
					shooter.starship,
				)
			)

			starship.addStatusEffect(
				StarshipStatusEffect(
					StarshipStatusEffectTypes.CRUISE_SLOW,
					speedPenalty,
					balancing.effectDurationMillis,
					shooter.starship,
				)
			)

			starship.userErrorAction("Ship speed slowed by ${(speedPenalty * 100).toInt()}%!")
		}
	}

	private fun curveEquation(x: Double, horizontalShift: Double) = (1.025.pow(x - horizontalShift) + 1) / 8

	companion object {
		private const val SEGMENT_LENGTH = 5
		private const val SHIFT_START = 100
	}
}
