package net.horizonsend.ion.server.features.transport.fluids.types.steam

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.FluidType.HeatingResult.Companion.HEATING_RATE_MULTIPLIER
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils.getFluidWeight
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.fluids.types.Water
import net.horizonsend.ion.server.miscellaneous.utils.centimetersCubedToLiters
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location

class Steam(
	key: IonRegistryKey<FluidType, out FluidType>,
	val prefix: Component,
	color: Color,
	heatCapacity: Double,
	pressureBars: Double = 1.0,
	val conversionResult: IonRegistryKey<FluidType, out FluidType>,
	val turbineResult: IonRegistryKey<FluidType, out FluidType>,
	val conversionCost: Double,
	val conversionTemperature: Double
) : GasFluid(key, color, heatCapacity, 18.01528, pressureBars) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS, FluidCategory.STEAM)

	override fun getDisplayName(stack: FluidStack): Component {
		return ofChildren(prefix, Component.text(" Steam"))
	}

	override fun getHeatingResult(stack: FluidStack, resultContainer: FluidStorageContainer, appliedEnergyJoules: Double, maximumTemperature: Double, location: Location?): HeatingResult {
		val currentTemperature = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, location).value

		val newTemperature = FluidUtils.getNewTemperature(stack, appliedEnergyJoules * HEATING_RATE_MULTIPLIER, maximumTemperature, location)

		if (newTemperature.value < conversionTemperature) {
			return HeatingResult.TemperatureIncreaseInPlace(newTemperature)
		}

		val convertTemperature = FluidProperty.Temperature(conversionTemperature)

		val deltaTemperature = conversionTemperature - currentTemperature
		val heatingJoules = getFluidWeight(stack, location) * Water.getIsobaricHeatCapacity(stack) * deltaTemperature

		val spareJoules = (appliedEnergyJoules - heatingJoules)
		val convertedGrams = spareJoules / conversionCost

		val stackDensity = getDensity(stack, location)

		val convertedVolume = centimetersCubedToLiters(convertedGrams / stackDensity)

		// Create a temp stack to get the density of the result
		val tempStack = FluidStack(conversionResult, 1.0).setData(FluidPropertyTypeKeys.TEMPERATURE, convertTemperature.clone())
		val resultDensity = conversionResult.getValue().getDensity(tempStack, location)

		// Get the shrinkage factor by multiplying by the fraction of the result density and current density, it will be below 1
		val contractionFactor = stackDensity / resultDensity

		val steamVolume = convertedVolume * contractionFactor

		// Consume water equal to weight boiled
		val consumed = minOf(convertedVolume, stack.amount)

		val steamStack = FluidStack(conversionResult, steamVolume)
			.setData(FluidPropertyTypeKeys.TEMPERATURE, convertTemperature.clone())

		return HeatingResult.Boiling(convertTemperature, steamStack, consumed)
	}
}
