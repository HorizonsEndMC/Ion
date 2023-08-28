package net.horizonsend.ion.server.features.starship.subsystem.reactor

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import kotlin.math.min
import kotlin.math.roundToInt

class ReactorSubsystem(
	starship: ActiveStarship
) : StarshipSubsystem(starship, starship.centerOfMassVec3i) {
	val output: Double =
		Math.cbrt(starship.initialBlockCount.coerceAtLeast(500).toDouble()) * 3000.0 * (starship.type.poweroverrider)
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
		val reactorOutput = this.output
		val shieldPortion = this.powerDistributor.shieldPortion
		val shieldEfficiency = starship.shieldEfficiency
		val shieldPower = reactorOutput * shieldPortion * shieldEfficiency * delta
		val totalMissing = starship.shields.sumOf { shield -> shield.maxPower - shield.power }

		if (totalMissing == 0) {
			return
		}

		for (shield in starship.shields) {
			val missing = shield.maxPower - shield.power

			if (missing == 0) {
				continue
			}

			val fraction = ((missing.toDouble() / totalMissing.toDouble()) * shieldPower).roundToInt()
			shield.power += min(missing, fraction)
		}

		if (starship is ActiveControlledStarship) {
			StarshipShields.updateShieldBars(starship)
		}
	}
}
