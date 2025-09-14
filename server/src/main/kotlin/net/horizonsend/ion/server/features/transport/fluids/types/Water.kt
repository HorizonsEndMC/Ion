package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils.getFluidWeight
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.PIPE_INTERIOR_PADDING
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.centimetersCubedToLiters
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import org.bukkit.Axis
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

object Water : FluidType(FluidTypeKeys.WATER) {
	override val categories: Array<FluidCategory> = arrayOf()

	override fun displayInPipe(world: World, origin: Vector, destination: Vector) {
		val trailOptions = Trail(
			/* target = */ destination.toLocation(world),
			/* color = */ Color.fromRGB(30, 60, 255),
			/* duration = */ 20
		)

		world.spawnParticle(Particle.TRAIL, origin.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trailOptions, false)
	}

	override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {
		val faceCenter = leakingNode.getCenter().add(leakingDirection.direction.multiply(0.5)).toLocation(world)

		when (leakingDirection.axis) {
			Axis.Y -> faceCenter.add(Vector(
				Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
				0.0,
				Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING)
			))
			else -> faceCenter.add(Vector(
				(Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING) * leakingDirection.modZ) + (leakingDirection.modX * 0.05),
				Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING / 2),
				(Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING) * leakingDirection.modX) + (leakingDirection.modZ * 0.05)
			))
		}

		world.spawnParticle(Particle.FALLING_WATER, faceCenter, 1, 0.0, 0.0, 0.0)
	}

	override fun getDisplayName(stack: FluidStack): Component {
		return text("Water", BLUE)
	}

	override fun getIsobaricHeatCapacity(stack: FluidStack): Double {
		return 4.181
	}

	override fun getMolarMass(stack: FluidStack): Double {
		return 18.01528
	}

	override fun getDensity(stack: FluidStack, location: Location?): Double {
		return 1.0
	}

	override fun getHeatingResult(stack: FluidStack, resultContainer: FluidStorageContainer, appliedEnergyJoules: Double, maximumTemperature: Double, location: Location?): HeatingResult {
		val currentTemperature = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, location).value

		val newTemperature = FluidUtils.getNewTemperature(stack, appliedEnergyJoules, maximumTemperature, location)
		val boilingPoint = getBoilingPoint(stack, location)

		if (newTemperature.value < boilingPoint) {
			return HeatingResult.TemperatureIncreaseInPlace(newTemperature)
		}

		val boilingTemperature = FluidProperty.Temperature(100.0)

		if (resultContainer.getRemainingRoom() <= 0) {
			return HeatingResult.Boiling(boilingTemperature, FluidStack.empty(), 0.0)
		}

		val deltaTemperature = boilingPoint - currentTemperature
		val heatingJoules = getFluidWeight(stack, location) * getIsobaricHeatCapacity(stack) * deltaTemperature

		val spareJoules = (appliedEnergyJoules - heatingJoules)
		val boiledGrams = spareJoules / LATENT_HEAT_OF_VAPORIZATION

		val tempSteamStack = FluidStack(FluidTypeKeys.STEAM, 1.0)
		val steamDensity = FluidUtils.getGasDensity(tempSteamStack, location)
		val steamVolume = minOf(resultContainer.getRemainingRoom(), centimetersCubedToLiters(boiledGrams / steamDensity))

		// Consume water equal to weight boiled
		val waterVolume = centimetersCubedToLiters(boiledGrams / this.getDensity(stack, location))
		val consumed = minOf(waterVolume, stack.amount)

		val steamStack = FluidStack(FluidTypeKeys.STEAM, steamVolume)
			.setData(FluidPropertyTypeKeys.TEMPERATURE, boilingTemperature.clone())

		return HeatingResult.Boiling(boilingTemperature, steamStack, consumed)
	}

	private fun getBoilingPoint(stack: FluidStack, location: Location?): Double {
//		val pressure = stack.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE.getValue(), location).value
		return 100.0
	}

	private const val EXPANSION_FACTOR = 12.0
	private val LATENT_HEAT_OF_VAPORIZATION get() = 2257 // j/g
}
