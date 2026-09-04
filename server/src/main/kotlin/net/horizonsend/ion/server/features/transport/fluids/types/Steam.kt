package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.DisplayProperties
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component
import org.bukkit.Color

class Steam(
	key: IonRegistryKey<FluidType, out FluidType>,
	val prefix: Component,
	color: Color,
	heatCapacity: Double,
	pressureBars: Double = 1.0
) : GasFluid(key, DisplayProperties(color, "transparent_gas")) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS, FluidCategory.STEAM)

	override fun getDisplayName(stack: FluidStack): Component {
		return ofChildren(prefix, Component.text(" Steam"))
	}
}
