package net.horizonsend.ion.server.features.transport.fluids.types

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.PIPE_INTERIOR_PADDING
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

class GasFluid(
	key: IonRegistryKey<FluidType, out FluidType>,
	private val gasKey: IonRegistryKey<Gas, out Gas>,
	val color: Color
) : FluidType(key) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS)

	override val displayName: Component get() = ofChildren(gas.displayName, text(" Gas", GRAY))

	val gas get() = gasKey.getValue()

	override fun displayInPipe(world: World, origin: Vector, destination: Vector) {
		val trailOptions = Trail(
			/* target = */ destination.toLocation(world),
			/* color = */ color,
			/* duration = */ 20
		)

		world.spawnParticle(Particle.TRAIL, origin.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trailOptions, false)
	}

	override fun playLeakEffects(world: World, leakingNode: FluidNode, leakingDirection: BlockFace) {
		val openLocation = getRelative(leakingNode.location, leakingDirection)

		val smokeLocation = toVec3i(openLocation).toCenterVector().toLocation(world).add(leakingDirection.direction.multiply(-0.5))

		val offset = Vector(
			Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
			Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
			Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING)
		)

		val start = smokeLocation.clone().add(offset)

		val destination = start.clone().add(offset).add(leakingDirection.direction.multiply(5)).add(windDirection.multiply(2))

		val trial = Trail(
			/* target = */ destination,
			/* color = */ color,
			/* duration = */ 40
		)

		world.spawnParticle(Particle.TRAIL, start, 1, 0.0, 0.0, 0.0, 2.125, trial, true)
	}

	companion object {
		//TODO
		val windDirection: Vector get() = Bukkit.getPlayer("GutinGongoozler")?.location?.direction ?: Vector.getRandom()
	}
}
