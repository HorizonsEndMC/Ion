package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.fluids.DisplayProperties
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY

class LegacyGasFluid(
	key: IonRegistryKey<FluidType, out FluidType>,
	private val gasKey: IonRegistryKey<Gas, out Gas>,
	displayProperties: DisplayProperties,
	heatCapacity: Double,
	molarMass: Double,
	pressureBars: Double = 1.0
) : GasFluid(key, displayProperties, heatCapacity, molarMass, pressureBars) {
	override fun getDisplayName(stack: FluidStack): Component {
		return ofChildren(gas.displayName, text(" Gas", GRAY))
	}

	val gas get() = gasKey.getValue()
}
