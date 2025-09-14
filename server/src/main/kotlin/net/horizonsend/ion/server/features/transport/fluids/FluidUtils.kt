package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.FluidDisplayModule.Companion.format
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature
import net.horizonsend.ion.server.miscellaneous.utils.celsiusToKelvin
import net.horizonsend.ion.server.miscellaneous.utils.litersToCentimetersCubed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location

object FluidUtils {
	const val GAS_CONSTANT = 0.08206

	fun formatFluidInfo(fluidStack: FluidStack): Component {
		val text = text()
		text.append(fluidStack.getDisplayName())
		text.append(Component.space(), bracketed(ofChildren(text(format.format(fluidStack.amount), NamedTextColor.GRAY), text("L", NamedTextColor.GRAY))))

		var lines = 0

		for ((key, property) in fluidStack.getDataMap()) {
			lines++

			text.append(Component.newline())
			text.append(text(" • ", HE_MEDIUM_GRAY))
			text.append(key.getDisplayName())
			text.append(text(": ", HE_DARK_GRAY))
			text.append(key.formatValueUnsafe(property))
		}

		return text.build()
	}

	/** Returns the weight of this fluid stack, in grams */
	fun getFluidWeight(fluid: FluidStack, location: Location?): Double {
		val density = fluid.type.getValue().getDensity(fluid, location)
		return density * litersToCentimetersCubed(fluid.amount)
	}

	/**
	 * Uses the ideal gas law to get the density of the gas, in grams / cm^3
	 **/
	fun getGasDensity(stack: FluidStack, location: Location?): Double {
		val type = stack.type.getValue()

		val temperatureCelsius = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value
		val pressureBars = stack.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE.getValue(), location).value

		val density = (type.getMolarMass(stack) * pressureBars) / (GAS_CONSTANT * celsiusToKelvin(temperatureCelsius))

		return density
	}

	fun getNewTemperature(fluidStack: FluidStack, appliedHeatJoules: Double, maximumTemperature: Double, location: Location?): Temperature {
		val currentHeat = fluidStack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value

		val fluidWeightGrams = getFluidWeight(fluidStack, location)
		val specificHeat = fluidStack.type.getValue().getIsobaricHeatCapacity(fluidStack)
		val kelvinHeat = appliedHeatJoules / (specificHeat * fluidWeightGrams)

		return Temperature(minOf(currentHeat + kelvinHeat, maximumTemperature))
	}
}
