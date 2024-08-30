package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import java.util.function.Supplier

class GasPipedFluid(
	identifier: String,
	private val gasSupplier: Supplier<Gas>,
) : PipedFluid(identifier) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS)

	val gas get() = gasSupplier.get()
}
