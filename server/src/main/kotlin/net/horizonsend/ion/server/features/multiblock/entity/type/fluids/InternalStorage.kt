package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

/**
 * Fluid storing multiblocks can have a single, or multiple fluid storage capacities
 *
 * Each internal storage can hold a single type of fluid
 **/
abstract class InternalStorage {
	protected var amountUnsafe: Int = 0
	protected var fluidUnsafe: PipedFluid? = null

	abstract fun canStore(resource: PipedFluid, liters: Double): Boolean

	abstract fun getCapacity(): Int

	fun getStoredFluid(): PipedFluid? = fluidUnsafe

	fun addAmount(amount: Int) {
		setAmount(getAmount() + amount)
	}

	fun getAmount(): Int = amountUnsafe

	fun setContents(fluid: PipedFluid, amount: Int) {
		setFluid(fluid)
		setAmount(amount)
	}

	fun setAmount(amount: Int) {
		val corrected = amount.coerceIn(0, getCapacity())

		this.amountUnsafe = corrected

		if (corrected == 0) setFluid(null)

		updateVisually()
	}

	fun setFluid(fluid: PipedFluid?) {
		this.fluidUnsafe = fluid

		updateVisually()
	}

	open fun updateVisually() {}

	fun getContents(): Pair<PipedFluid?, Int> = fluidUnsafe to getAmount()
}
