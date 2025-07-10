package net.horizonsend.ion.server.features.starship.subsystem.reactor

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import java.time.Duration
import kotlin.math.min
import kotlin.math.roundToInt

class ReactorSubsystem(
	starship: ActiveStarship,
	powerModifier: Double
) : StarshipSubsystem(starship, starship.centerOfMass) {

	companion object {
		const val OVERSIZE_POWER_PENALTY = 0.5
	}

	val output: Double =
		Math.cbrt(starship.initialBlockCount.coerceAtLeast(500).toDouble()) * 3000.0 * (starship.type.powerOverrider) * powerModifier
	val powerDistributor = PowerDistributor()
	val weaponCapacitor = WeaponCapacitor(this)
	val heavyWeaponBooster = HeavyWeaponBooster(this)

	override fun isIntact(): Boolean {
		return true
	}

	fun tick(delta: Double) {
		chargeShields(delta)
		weaponCapacitor.charge(delta)
	}

	private fun chargeShields(delta: Double) {
		if (starship.reserveShieldPower == 0) return
		val reactorOutput = this.output
		val shieldPortion = this.powerDistributor.shieldPortion
		val shieldEfficiency = starship.shieldEfficiency
		val shieldPower = reactorOutput * shieldPortion * shieldEfficiency * delta/* *
				if (starship.initialBlockCount < 1000)
					// approx. 0.25 shield regen at 150 blocks, 0.62 regen at 500, and 1.0 at 1k+
					-0.0953256 * cbrt(starship.initialBlockCount.toDouble()) + 0.0617674 * sqrt(starship.initialBlockCount.toDouble())
				else 1.0
				*/
		val totalMissing = starship.shields.sumOf { shield -> shield.maxPower - shield.power }

		if (totalMissing == 0) {
			return
		}

		for (shield in starship.shields) {
			// skip this shield if the time between now and when this shield was last hit is less than a minute
			if (Duration.ofMillis(System.currentTimeMillis() - shield.lastTimeThisShieldWasDamaged) < Duration.ofMinutes(1)) continue
			val missing = shield.maxPower - shield.power

			if (missing == 0) {
				continue
			}

			val fraction = ((missing.toDouble() / totalMissing.toDouble()) * shieldPower).roundToInt()
			val healthRestored = min(missing, fraction)
			shield.power += healthRestored
			starship.reserveShieldPower -= healthRestored
		}

		if (starship is ActiveControlledStarship) {
			StarshipShields.updateShieldBars(starship)
		}
	}
}
