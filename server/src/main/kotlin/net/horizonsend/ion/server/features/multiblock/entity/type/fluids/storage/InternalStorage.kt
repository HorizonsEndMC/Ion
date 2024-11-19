package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry.EMPTY
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Fluid storing multiblocks can have a single, or multiple fluid storage capacities
 *
 * Each internal storage can hold a single type of fluid
 **/
abstract class InternalStorage {
	private var amountUnsafe: Int = 0
	protected open var fluidUnsafe: Fluid = EMPTY

	abstract val inputAllowed: Boolean
	abstract val extractionAllowed: Boolean

	protected val mutex: Any = Any()

	fun getFluidType(): Fluid = synchronized(mutex) { fluidUnsafe }
	fun getAmount(): Int = synchronized(mutex) { amountUnsafe }

	abstract fun getCapacity(): Int

	fun remainingCapacity() = getCapacity() - getAmount()

	abstract fun canStore(fluid: FluidStack): Boolean

	abstract fun canStore(type: Fluid): Boolean

	fun isEmpty(): Boolean = getFluidType() == EMPTY || getAmount() == 0

	fun isFull(): Boolean = getFluidType() != EMPTY && getAmount() == getCapacity()

	fun removeAmount(amount: Int): Int {
		val newAmount = synchronized(mutex) {
			val newTotal = amountUnsafe - amount
			val corrected = newTotal.coerceIn(0, getCapacity())

			amountUnsafe = corrected
			newTotal
		}

		runUpdates()

		// negative total will be a remainder
		return if (newAmount < 0) -newAmount else 0
	}

	fun addAmount(amount: Int): Int {
		val newAmount = synchronized(mutex) {
			val newAmount = amountUnsafe + amount
			val corrected = newAmount.coerceIn(0, getCapacity())

			amountUnsafe = corrected
			newAmount
		}

		runUpdates()

		return if (newAmount > getCapacity()) newAmount - getCapacity() else 0
	}

	fun setContents(fluid: FluidStack) {
		setFluid(fluid.type)
		setAmount(fluid.amount)
	}

	fun setAmount(amount: Int) {
		val new = synchronized(mutex) {
			val corrected = amount.coerceIn(0, getCapacity())

			this.amountUnsafe = corrected
			corrected
		}

		if (new == 0) setFluid(EMPTY)

		runUpdates()
	}

	fun setFluid(fluid: Fluid) {
		synchronized(mutex) {
			this.fluidUnsafe = fluid
		}

		runUpdates()
	}

	/**
	 * Load storage data from the provided persistent data container
	 **/
	fun loadData(pdc: PersistentDataContainer) {
		val fluid = pdc.get(NamespacedKeys.FLUID, PersistentDataType.STRING)?.let { FluidRegistry[it] } ?: EMPTY
		val amount = pdc.getOrDefault(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.INTEGER, 0)

		setFluid(fluid)
		setAmount(amount)
	}

	/**
	 * Save storage data to the provided persistent data container
	 **/
	fun saveData(destination: PersistentDataContainer) {
		val fluid = getFluidType()
		val amount = getAmount()

		destination.set(NamespacedKeys.FLUID, PersistentDataType.STRING, fluid.identifier)
		destination.set(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.INTEGER, amount)
	}

	private val updateHandlers = mutableListOf<(InternalStorage) -> Unit>()

	fun registerUpdateHandler(handler: (InternalStorage) -> Unit) {
		updateHandlers.add(handler)
	}

	fun removeUpdateHandler(handler: (InternalStorage) -> Unit) {
		updateHandlers.remove(handler)
	}

	fun getUpdateHandlers(): List<(InternalStorage) -> Unit> = updateHandlers

	/** Notify update handlers of an update */
	fun runUpdates() {
		updateHandlers.toList().forEach { it.invoke(this) }
	}

	override fun toString(): String {
		return "${javaClass.simpleName}[capacity= ${getCapacity()} fluid= (${getFluidType()}, ${getAmount()})]"
	}
}
