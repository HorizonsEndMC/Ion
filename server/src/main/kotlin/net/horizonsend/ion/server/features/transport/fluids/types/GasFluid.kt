package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import java.util.function.Supplier

class GasFluid(
	private val gasSupplier: Supplier<Gas>
) : Fluid() {
	val gas get() = gasSupplier.get()
}
