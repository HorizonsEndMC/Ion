package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidType.HeatingResult.Companion.HEATING_RATE_MULTIPLIER
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

abstract class FluidType(override val key: IonRegistryKey<FluidType, out FluidType>) : Keyed<FluidType> {
	abstract val displayProperties: DisplayProperties

	abstract val categories: Array<FluidCategory>

	/**
	 * Effect played inside a pipe, when this fluid is present
	 **/
	abstract fun displayInPipe(world: World, origin: Vector, destination: Vector)

	/**
	 * Effect played when leaking form a pipe, when this fluid is leaking
	 **/
	abstract fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace)

	/**
	 * Called when this fluid leaks from a pipe. Should be used for anything that is not a visual effect, such as pollution.
	 **/
	open fun onLeak(world: World, location: Vec3i, amount: Double) {}

	abstract fun getDisplayName(stack: FluidStack): Component

	/**
	 * Returns the heat capacity of this fluid, in joules per gram
	 **/
	abstract fun getIsobaricHeatCapacity(stack: FluidStack): Double

	/**
	 * Returns the density of this fluid, in grams per cubic centimeter
	 **/
	abstract fun getDensity(stack: FluidStack, location: Location?): Double

	/**
	 * Returns the molar mass of this fluid, in grams per mole
	 **/
	abstract fun getMolarMass(): Double

	open val defaultProperties: Map<IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>>, FluidProperty> = emptyMap()

	/**
	 * Returns result when a fluid stack of this type is heated
	 *
	 * @param stack: The fluid stack being heated
	 * @param resultContainer: The container holding the result
	 * @param appliedEnergyJoules The amount of energy applied to the fluid, in joules
	 * @param maximumTemperature The maximum temperature this fluid can be heated to
	 * @param location: The location where this fluid is being heated, used to acertain default values
	 **/
	open fun getHeatingResult(stack: FluidStack, resultContainer: FluidStorageContainer, appliedEnergyJoules: Double, maximumTemperature: Double, location: Location?): HeatingResult {
		val newTemperature = FluidUtils.getNewTemperature(stack, appliedEnergyJoules * HEATING_RATE_MULTIPLIER, maximumTemperature, location)
		return HeatingResult.TemperatureIncreasePassthrough(newTemperature)
	}

	sealed class HeatingResult(val newTemperature: FluidProperty.Temperature) {
		class TemperatureIncreaseInPlace(newTemperature: FluidProperty.Temperature): HeatingResult(newTemperature)
		class TemperatureIncreasePassthrough(newTemperature: FluidProperty.Temperature): HeatingResult(newTemperature)

		class Boiling(
			newTemperature: FluidProperty.Temperature,
			val newFluidStack: FluidStack,
			val inputRemovalAmount: Double
		) : HeatingResult(newTemperature)

		companion object {
			/** A global multiplier for heating rates, since realistic numbers are annoyingly slow **/
			const val HEATING_RATE_MULTIPLIER = 10.0
		}
	}
}
