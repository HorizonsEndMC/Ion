package net.horizonsend.ion.server.features.transport.fluids.types.steam

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.kyori.adventure.text.Component
import org.bukkit.Color

object SuperDenseSteam : GasFluid(
	key = FluidTypeKeys.DENSE_STEAM,
	color = Color.WHITE,
	heatCapacity = 2.030,
	molarMass = 18.01528,
	pressureBars = 20.0
) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS, FluidCategory.STEAM)

	override fun getDisplayName(stack: FluidStack): Component {
		if (stack.hasData(FluidPropertyTypeKeys.TEMPERATURE)) {
			val boiling = stack.getData(FluidPropertyTypeKeys.TEMPERATURE.getValue())?.value?.let { it > 100.0 }
			if (boiling == true) return Component.text("Dry Steam")
		}

		return Component.text("Super Steam")
	}
}
