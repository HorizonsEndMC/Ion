package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import java.util.function.Supplier

class GasFluid(
	key: IonRegistryKey<FluidType, out FluidType>,
	private val gasSupplier: Supplier<Gas>,
) : FluidType(key) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS)

	override val displayName: Component get() = ofChildren(gas.displayName, text(" Gas", GRAY))

	val gas get() = gasSupplier.get()
}
