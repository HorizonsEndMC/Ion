package net.horizonsend.ion.server.features.multiblock.entity.type

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	val position: BlockKey
	var powerUnsafe: Int
	val maxPower: Int

	fun setPower(amount: Int) {
		val correctedPower = amount.coerceIn(0, maxPower)

		powerUnsafe = correctedPower

		//TODO better solution
		powerUpdateScope.launch { updatePowerVisually() }
	}

	fun getPower(): Int {
		return powerUnsafe
	}

	/**
	 * Returns the amount of power that could not be added
	 **/
	fun addPower(amount: Int): Int {
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
		private val prefixComponent = text("E: ", NamedTextColor.YELLOW)
		private val powerUpdateScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	}

	/** Store power data */
	fun storePower(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, powerUnsafe)
	}

	suspend fun updatePowerVisually() {
		//TODO replace this
		require(this is MultiblockEntity)
		Tasks.sync {
			val sign = getSign() ?: return@sync
			sign.front().line(2, formatPower())
			sign.update()
		}
	}

	fun formatPower(): Component = ofChildren(prefixComponent, text(powerUnsafe, GREEN))
}
