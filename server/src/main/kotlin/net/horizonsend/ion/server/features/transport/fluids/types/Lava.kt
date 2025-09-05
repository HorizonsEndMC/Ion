package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.PIPE_INTERIOR_PADDING
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Axis
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

object Lava : FluidType(FluidTypeKeys.LAVA) {
	override val categories: Array<FluidCategory> = arrayOf()
	override val displayName: Component = text("Lava", HE_LIGHT_ORANGE)

	override fun displayInPipe(world: World, origin: Vector, destination: Vector) {
		val colors = setOf(
			Color.fromRGB(Integer.parseInt("F5451D", 16)),
			Color.fromRGB(Integer.parseInt("FC5C38", 16)),
			Color.fromRGB(Integer.parseInt("DE2E07", 16))
		)

		val trailOptions = Trail(
			/* target = */ destination.toLocation(world),
			/* color = */ colors.random(),
			/* duration = */ 20
		)

		world.spawnParticle(Particle.TRAIL, origin.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trailOptions, false)
	}

	override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {
		val faceCenter = leakingNode.getCenter().add(leakingDirection.direction.multiply(0.5)).toLocation(world)

		if (testRandom(0.05)) world.spawnParticle(Particle.LAVA, faceCenter, 1, 0.0, 0.0, 0.0)

		when (leakingDirection.axis) {
			Axis.Y -> faceCenter.add(Vector(
				Random.nextDouble(-PIPE_INTERIOR_PADDING * 1.6, PIPE_INTERIOR_PADDING * 1.6),
				0.0,
				Random.nextDouble(-PIPE_INTERIOR_PADDING * 1.6, PIPE_INTERIOR_PADDING * 1.6)
			))
			else -> faceCenter.add(Vector(
				(Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING) * leakingDirection.modZ) + (leakingDirection.modX * 0.05),
				Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING / 2),
				(Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING) * leakingDirection.modX) + (leakingDirection.modZ * 0.05)
			))
		}

		world.spawnParticle(Particle.FALLING_LAVA, faceCenter, 1, 0.0, 0.0, 0.0)
	}
}
