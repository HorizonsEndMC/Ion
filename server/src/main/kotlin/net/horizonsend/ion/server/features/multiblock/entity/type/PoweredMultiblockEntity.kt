package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	var power: Int
	val type: PowerStoringMultiblock

	val maxPower get() = type.maxPower

	fun setPower(amount: Int) {
		val correctedPower = amount.coerceIn(0, maxPower)

		power = correctedPower
	}

	fun getPower(): Int {
		return power
	}

	/**
	 * Returns the amount of power that could not be added
	 **/
	fun addPower(sign: Sign, amount: Int): Int {
		val newAmount = getPower() + amount

		setPower(newAmount)

		return if (newAmount > maxPower) maxPower - newAmount else 0
	}

	/**
	 * Returns the amount of power that could not be removed
	 **/
	fun removePower(amount: Int): Int {
		val newAmount = getPower() - amount

		setPower(newAmount)

		return if (newAmount < 0) newAmount else 0
	}

	/**
	 * Returns whether this multiblock has the capacity to fit the specified amount of power
	 **/
	fun canFitPower(amount: Int): Boolean {
		return getPower() + amount < maxPower
	}

	/**
	 * Returns true if this amount of power can be removed without reaching zero.
	 **/
	fun canRemovePower(amount: Int): Boolean {
		return getPower() - amount > 0
	}

	companion object {
		private val prefixComponent = Component.text("E: ", NamedTextColor.YELLOW)
	}

	/** Store power data */
	fun storePower(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, power)
	}
}
