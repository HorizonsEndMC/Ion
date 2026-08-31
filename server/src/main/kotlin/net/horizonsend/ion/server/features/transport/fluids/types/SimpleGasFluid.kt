package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import kotlin.random.Random

class SimpleGasFluid(
	key: IonRegistryKey<FluidType, out FluidType>,
	override val displayName: Component,
	private val color: Color,
	private val leakDistance: Double = 5.0
) : FluidType(key) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS)

	override fun displayInPipe(world: World, origin: Vector, destination: Vector) {
		val trailOptions = Trail(
			destination.toLocation(world),
			color,
			20
		)

		world.spawnParticle(Particle.TRAIL, origin.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trailOptions, false)
	}

	override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {
		val forward = leakingDirection.direction.normalize()
		val reference = if (forward.y != 0.0) Vector(1.0, 0.0, 0.0) else Vector(0.0, 1.0, 0.0)
		val right = forward.clone().crossProduct(reference).normalize()
		val up = right.clone().crossProduct(forward).normalize()

		val azimuth = Random.nextDouble(0.0, PI * 2.0)
		val spread = Random.nextDouble() * tan(MAXIMUM_LEAK_ANGLE_RADIANS)
		val randomizedDirection = forward.clone()
			.add(right.clone().multiply(cos(azimuth) * spread))
			.add(up.clone().multiply(sin(azimuth) * spread))
			.normalize()

		val start = leakingNode.getCenter()
			.add(forward.clone().multiply(0.5))
			.add(right.clone().multiply(Random.nextDouble(-START_POSITION_VARIATION, START_POSITION_VARIATION)))
			.add(up.clone().multiply(Random.nextDouble(-START_POSITION_VARIATION, START_POSITION_VARIATION)))
			.toLocation(world)
		val randomizedDistance = leakDistance * Random.nextDouble(MINIMUM_DISTANCE_MULTIPLIER, MAXIMUM_DISTANCE_MULTIPLIER)
		val destination = start.clone().add(randomizedDirection.multiply(randomizedDistance))
		val duration = Random.nextInt(MINIMUM_LEAK_DURATION_TICKS, MAXIMUM_LEAK_DURATION_TICKS + 1)
		val trailOptions = Trail(destination, color, duration)

		world.spawnParticle(Particle.TRAIL, start, 1, 0.0, 0.0, 0.0, 0.0, trailOptions, true)
	}

	companion object {
		private val MAXIMUM_LEAK_ANGLE_RADIANS = Math.toRadians(30.0)
		private const val START_POSITION_VARIATION = 0.08
		private const val MINIMUM_DISTANCE_MULTIPLIER = 0.75
		private const val MAXIMUM_DISTANCE_MULTIPLIER = 1.25
		private const val MINIMUM_LEAK_DURATION_TICKS = 30
		private const val MAXIMUM_LEAK_DURATION_TICKS = 60
	}
}
