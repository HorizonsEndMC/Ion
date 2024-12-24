package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.time.Duration

class IonTurretProjectile(
	ship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	override val speed: Double,
	override val color: Color,
	override val range: Double,
	override val particleThickness: Double,
	override val explosionPower: Float,
	override val starshipShieldDamageMultiplier: Double,
	override val areaShieldDamageMultiplier: Double,
	override val soundName: String,
	override val balancing: StarshipWeapons.ProjectileBalancing?,
	shooter: Damager

): LaserProjectile(ship, name, loc, dir, shooter) {

	override val volume: Int = (range / 16).toInt()

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = dir.clone().normalize().multiply(travel)
		val particle = Particle.DUST
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)

		helixAroundVector(oldLocation, vector, 0.3, 20, wavelength = 1.0) {
			loc.world.spawnParticle(
				Particle.WAX_OFF,
				it,
				0,
				0.0,
				0.0,
				0.0,
				0.0,
				null,
				true
			)
		}

		loc.world.spawnParticle(particle, loc.x, loc.y, loc.z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, true)
	}

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		val shipsThrusters = starship.thrusters

		for (thruster in shipsThrusters) {
			if (impactLocation.distance(thruster.pos.toLocation(starship.world)) > 8) continue

			thruster.lastIonTurretLimited = System.currentTimeMillis()
		}

		starship.userErrorAction("Direct Control speed slowed by ${(SLOW_FACTOR * 100).toInt()}%!")
		// starship was not slowed by ion turrets recently
		if (starship.directControlSlowExpiryFromIonTurrets < System.currentTimeMillis()) {
			// Only start the timer based on the first hit
			starship.directControlSlowExpiryFromIonTurrets = System.currentTimeMillis() + Duration.ofSeconds(SLOW_DURATION_SECONDS).toMillis()
		}
		// Only apply slow if this ship has not been hit within the last 1 second
		if (starship.lastTimeThisShipWasHitByAnIonTurretAndTheSlowEffectHappened + Duration.ofSeconds(1L).toMillis() <= System.currentTimeMillis()) {
			// Reduce starship speed by the slow factor
			starship.directControlSpeedModifierFromIonTurrets *= (1 - SLOW_FACTOR)
			starship.lastTimeThisShipWasHitByAnIonTurretAndTheSlowEffectHappened = System.currentTimeMillis()
		}

		Tasks.syncDelay(Duration.ofSeconds(SLOW_DURATION_SECONDS).toSeconds() * 20L) {
			// reset for individual shots
			// starship.directControlSpeedModifier /= (1 - SLOW_FACTOR)
			if (ActiveStarships.isActive(starship) && starship.directControlSlowExpiryFromIonTurrets - 100 <= System.currentTimeMillis()) {
				// hard reset to normal speed (I feel that weird double-rounding bugs might be possible)
				starship.directControlSpeedModifierFromIonTurrets = 1.0
				starship.directControlSlowExpiryFromIonTurrets = 0L
				starship.lastTimeThisShipWasHitByAnIonTurretAndTheSlowEffectHappened = 0L
				starship.informationAction("Direct Control speed restored")
			}
		}
	}

	companion object {
		private const val SLOW_FACTOR = 0.1
		private const val SLOW_DURATION_SECONDS = 7L
	}
}
