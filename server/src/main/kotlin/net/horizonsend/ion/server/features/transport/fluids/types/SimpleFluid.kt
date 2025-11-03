package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.DisplayProperties
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.PIPE_INTERIOR_PADDING
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.kyori.adventure.text.Component
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

class SimpleFluid(
	key: IonRegistryKey<FluidType, out FluidType>,
	val displayName: Component,
	override val displayProperties: DisplayProperties,
	override val categories: Array<FluidCategory>,
	private val heatCapacity: Double,
	private val molarMass: Double,
	private val density: Double,
	override val defaultProperties: Map<IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>>, FluidProperty> = emptyMap()
) : FluidType(key) {
	override fun displayInPipe(world: World, origin: Vector, destination: Vector) {
		val trailOptions = Trail(
			/* target = */ destination.toLocation(world),
			/* color = */ displayProperties.color,
			/* duration = */ 20
		)

		world.spawnParticle(Particle.TRAIL, origin.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trailOptions, false)
	}

	override fun getDisplayName(stack: FluidStack): Component {
		return displayName
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
				Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING) * leakingDirection.modZ,
				0.0,
				Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING) * leakingDirection.modX
			))
		}

		world.spawnParticle(Particle.FALLING_WATER, faceCenter, 1, 0.0, 0.0, 0.0)
	}

	override fun getIsobaricHeatCapacity(stack: FluidStack): Double {
		return heatCapacity * stack.amount
	}

	override fun getMolarMass(): Double {
		return molarMass
	}

	override fun getDensity(stack: FluidStack, location: Location?): Double {
		return density
	}
}
