package net.starlegacy.feature.starship.subsystem.reactor

import kotlin.math.min

class WeaponCapacitor(val subsystem: ReactorSubsystem) {
	val capacity: Double = subsystem.output / 6
	var charge: Double = capacity
		set(value) {
			field = value.coerceIn(0.0, capacity)
		}

	fun isAvailable(amount: Double): Boolean {
		return amount <= charge
	}

	fun tryConsume(amount: Double): Boolean {
		if (amount > charge) {
			return false
		}

		charge -= amount
		return true
	}

	fun charge(delta: Double) {
		charge = min(capacity, charge + subsystem.output * subsystem.powerDistributor.weaponPortion * delta)
	}
}
