package net.horizonsend.ion.server.features.transport.fluids.types.steam

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
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

object DenseSteam : GasFluid(
	key = FluidTypeKeys.DENSE_STEAM,
	color = Color.WHITE,
	heatCapacity = 2.030,
	molarMass = 18.01528,
	pressureBars = 5.0
) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS, FluidCategory.STEAM)

	override fun getDisplayName(stack: FluidStack): Component {
		return Component.text("Dense Steam")
	}

	override fun getHeatingResult(stack: FluidStack, resultContainer: FluidStorageContainer, appliedEnergyJoules: Double, maximumTemperature: Double, location: Location?): HeatingResult {
		val currentTemperature = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, location).value

		val newTemperature = FluidUtils.getNewTemperature(stack, appliedEnergyJoules * HEATING_RATE_MULTIPLIER, maximumTemperature, location)

		if (newTemperature.value < CONVERSION_POINT) {
			return HeatingResult.TemperatureIncreaseInPlace(newTemperature)
		}

		val boilingTemperature = FluidProperty.Temperature(CONVERSION_POINT)

		val deltaTemperature = CONVERSION_POINT - currentTemperature
		val heatingJoules = getFluidWeight(stack, location) * Water.getIsobaricHeatCapacity(stack) * deltaTemperature

		val spareJoules = (appliedEnergyJoules - heatingJoules)
		val convertedGrams = spareJoules / CONVERSION_COST

		val convertedVolume = centimetersCubedToLiters(convertedGrams / this.getDensity(stack, location))

		// Create a temp stack to get the density of the result
		val tempStack = FluidTypeKeys.SUPER_DENSE_STEAM.getValue().getDensity(FluidStack(FluidTypeKeys.SUPER_DENSE_STEAM, 1.0), location)
		// Get the shrinkage factor by multiplying by the fraction of the result density and current density, it will be below 1
		val contractionFactor = tempStack / getDensity(stack, location)

		val steamVolume = convertedVolume * contractionFactor

		// Consume water equal to weight boiled
		val consumed = minOf(convertedVolume, stack.amount)

		val steamStack = FluidStack(FluidTypeKeys.SUPER_DENSE_STEAM, steamVolume)
			.setData(FluidPropertyTypeKeys.TEMPERATURE, boilingTemperature.clone())

		return HeatingResult.Boiling(boilingTemperature, steamStack, consumed)
	}

	private const val CONVERSION_POINT = 450.0
	private const val CONVERSION_COST = 2257.0 * 2
}
