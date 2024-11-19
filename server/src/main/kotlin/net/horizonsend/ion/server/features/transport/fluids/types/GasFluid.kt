package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component
import java.util.function.Supplier

class GasFluid(
	identifier: String,
	private val gasSupplier: Supplier<Gas>,
) : Fluid(identifier) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS)

	override val displayName: Component get() = gas.displayName
	val gas get() = gasSupplier.get()
}
