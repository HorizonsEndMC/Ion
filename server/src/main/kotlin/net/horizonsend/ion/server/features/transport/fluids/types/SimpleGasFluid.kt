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
		val start = leakingNode.getCenter()
			.add(leakingDirection.direction.multiply(0.5))
			.toLocation(world)
		val destination = start.clone().add(leakingDirection.direction.multiply(leakDistance))
		val trailOptions = Trail(destination, color, 40)

		world.spawnParticle(Particle.TRAIL, start, 1, 0.0, 0.0, 0.0, 0.0, trailOptions, true)
	}
}
