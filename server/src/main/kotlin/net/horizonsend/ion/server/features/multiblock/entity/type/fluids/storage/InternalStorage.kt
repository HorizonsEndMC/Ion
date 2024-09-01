package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Fluid storing multiblocks can have a single, or multiple fluid storage capacities
 *
 * Each internal storage can hold a single type of fluid
 **/
abstract class InternalStorage {
	protected var amountUnsafe: Int = 0
	protected var fluidUnsafe: PipedFluid? = null

	abstract fun getCapacity(): Int

	abstract fun canStore(resource: PipedFluid, liters: Int): Boolean

	fun isEmpty(): Boolean = getStoredFluid() == null || getAmount() == 0

	fun isFull(): Boolean = getStoredFluid() != null && getAmount() == getCapacity()

	fun remove(amount: Int): Int {
		val newTotal = getAmount() - amount
		// negative total will be a remainder
		val notRemoved = if (newTotal < 0) -newTotal else 0

		setAmount(newTotal)

		return notRemoved
	}

	fun remove(fluid: PipedFluid, amount: Int): Int {
		if (getStoredFluid() != null && fluid != getStoredFluid()) return amount

		if (getStoredFluid() == null) setFluid(fluid)

		return remove(amount)
	}

	fun addAmount(amount: Int): Int {
		val newTotal = getAmount() + amount
		val notAdded = if (newTotal > getCapacity()) newTotal - getCapacity() else 0

		setAmount(getAmount() + amount)

		return notAdded
	}

	fun addAmount(fluid: PipedFluid, amount: Int): Int {
		if (getStoredFluid() != null && fluid != getStoredFluid()) return amount

		if (getStoredFluid() == null) setFluid(fluid)

		return addAmount(amount)
	}

	fun getStoredFluid(): PipedFluid? = fluidUnsafe

	fun getAmount(): Int = amountUnsafe

	fun setContents(fluid: PipedFluid, amount: Int) {
		setFluid(fluid)
		setAmount(amount)
	}

	fun setAmount(amount: Int) {
		val corrected = amount.coerceIn(0, getCapacity())

		this.amountUnsafe = corrected

		if (corrected == 0) setFluid(null)
	}

	fun setFluid(fluid: PipedFluid?) {
		this.fluidUnsafe = fluid
	}

	/**
	 * Load storage data from the provided persistent data container
	 **/
	fun loadData(pdc: PersistentDataContainer) {
		val fluid = pdc.get(NamespacedKeys.FLUID, PersistentDataType.STRING)?.let { TransportedFluids[it] }
		val amount = pdc.getOrDefault(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.INTEGER, 0)

		setFluid(fluid)
		setAmount(amount)
	}

	/**
	 * Save storage data to the provided persistent data container
	 **/
	fun saveData(destination: PersistentDataContainer) {
		val fluid = getStoredFluid() ?: return
		val amount = getAmount()

		destination.set(NamespacedKeys.FLUID, PersistentDataType.STRING, fluid.identifier)
		destination.set(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.INTEGER, amount)
	}

	override fun toString(): String {
		return "${javaClass.simpleName}[capacity= ${getCapacity()} fluid= (${getStoredFluid()}, ${getAmount()})]"
	}
}
