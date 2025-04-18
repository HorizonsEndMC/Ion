package net.horizonsend.ion.server.features.multiblock.entity.type.power

import java.util.concurrent.ConcurrentHashMap

class PowerStorage(val holder: PoweredMultiblockEntity, amount: Int, val capacity: Int) {
	private var backingPower = amount
		@Synchronized get
		@Synchronized set

	private val mutex = Any()

	fun isEmpty() = getPower() <= 0
	fun isFull() = getPower() >= capacity

	fun setPower(amount: Int) {
		synchronized(mutex) {
			val correctedPower = amount.coerceIn(0, capacity)

			backingPower = correctedPower
		}

		runUpdates()
	}

	fun getPower(): Int {
		return synchronized(mutex) { backingPower }
	}

	/**
	 * Returns the amount of power that could not be added
	 **/
	fun addPower(amount: Int): Int {
		val newAmount = synchronized(mutex) {
			val newAmount = backingPower + amount
			val corrected = newAmount.coerceIn(0, capacity)

			backingPower = corrected
			newAmount
		}

		runUpdates()

		return if (newAmount > capacity) newAmount - capacity else 0
	}

	/**
	 * Returns the amount of power that could not be removed
	 **/
	fun removePower(amount: Int): Int {
		val newAmount = synchronized(mutex) {
			val newAmount = backingPower - amount
			val corrected = newAmount.coerceIn(0, capacity)

			backingPower = corrected
			newAmount
		}

		runUpdates()

		return if (newAmount < 0) newAmount else 0
	}

	/**
	 * Returns whether this multiblock has the capacity to fit the specified amount of power
	 **/
	fun canFitPower(amount: Int): Boolean {
		return getPower() + amount < capacity
	}

	/**
	 * Returns true if this amount of power can be removed without reaching zero.
	 **/
	fun canRemovePower(amount: Int): Boolean {
		return getPower() - amount > 0
	}

	fun getRemainingCapacity(): Int {
		return capacity - getPower()
	}

	private val updateHandlers = ConcurrentHashMap.newKeySet<(PowerStorage) -> Unit>()

	fun registerUpdateHandler(handler: (PowerStorage) -> Unit) {
		updateHandlers.add(handler)
	}

	fun removeUpdateHandler(handler: (PowerStorage) -> Unit) {
		updateHandlers.remove(handler)
	}

	fun getUpdateHandlers(): List<(PowerStorage) -> Unit> = updateHandlers.toList()

	/** Notify update handlers of an update */
	fun runUpdates() {
		getUpdateHandlers().forEach { it.invoke(this) }
	}
}
