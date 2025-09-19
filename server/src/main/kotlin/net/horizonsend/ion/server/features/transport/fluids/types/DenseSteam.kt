package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.Location

object DenseSteam : GasFluid(
	key = FluidTypeKeys.DENSE_STEAM,
	color = Color.WHITE,
	heatCapacity = 2.030,
	molarMass = 18.01528,
	pressureBars = 5.0
) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS, FluidCategory.STEAM)

	override fun getDisplayName(stack: FluidStack): Component {
		if (stack.hasData(FluidPropertyTypeKeys.TEMPERATURE)) {
			val boiling = stack.getData(FluidPropertyTypeKeys.TEMPERATURE.getValue())?.value?.let { it > 100.0 }
			if (boiling == true) return text("Dry Steam")
		}

		return text("Steam")
	}

	override fun getDensity(stack: FluidStack, location: Location?): Double {
		// https://www.spiraxsarco.com/resources-and-design-tools/steam-tables/superheated-steam-region?sc_lang=en-GB
		return 1.13607
	}
}
