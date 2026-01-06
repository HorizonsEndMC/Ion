package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.WebifierBalancing.WebifierProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.WebifierStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.WebifierWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.time.Duration
import kotlin.math.pow

class WebifierProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	private val subsystem: WebifierWeaponSubsystem
) : LaserProjectile<WebifierProjectileBalancing>(source, name, loc, dir, shooter, WebifierStarshipWeaponMultiblock.damageType) {
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
		val speedPenalty = 0.55

		starship.userErrorAction("Ship speed slowed by ${(0.55 * 100).toInt()}%!")
		starship.directControlSlowExpiryFromWebifier = System.currentTimeMillis() + Duration.ofSeconds(5).toMillis()
		starship.directControlSpeedModifierFromWebifiers *= (1 - speedPenalty)
		starship.webifierCruiseSpeedMod *= (1-speedPenalty)

		Tasks.syncDelay(Duration.ofSeconds(5).toSeconds() * 20L) {
			// reset for individual shots
			starship.directControlSpeedModifierFromWebifiers /= (1 - speedPenalty)
			starship.webifierCruiseSpeedMod /= (1 - speedPenalty)
			if (ActiveStarships.isActive(starship) && starship.directControlSlowExpiryFromWebifier - 100 < System.currentTimeMillis()) {
				// hard reset to normal speed (I feel that weird double-rounding bugs might be possible)
				starship.directControlSpeedModifierFromWebifiers = 1.0
				starship.webifierCruiseSpeedMod = 1.0
				starship.directControlSlowExpiryFromWebifier = 0L
				starship.informationAction("Ship speed restored")
			}
		}
	}

	override fun playCustomSound(loc: Location, nearSound: SoundInfo, farSound: SoundInfo) { /* Do nothing */ }

	private fun curveEquation(x: Double, horizontalShift: Double) = (1.025.pow(x - horizontalShift) + 1) / 8

	companion object {
		private const val SEGMENT_LENGTH = 5
		private const val SHIFT_START = 100
	}
}
