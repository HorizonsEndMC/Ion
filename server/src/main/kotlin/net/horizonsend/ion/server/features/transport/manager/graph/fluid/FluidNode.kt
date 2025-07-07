package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Axis
import org.bukkit.block.BlockFace

abstract class FluidNode(val volume: Double) : TransportNode {
	private lateinit var graph: FluidNetwork

	abstract val flowCapacity: Double

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as FluidNetwork
	}

	class RegularJunctionPipe(override val location: BlockKey) : FluidNode(10.0) {
		override val flowCapacity: Double = 10.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PIPE_JUNCTION
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<RegularJunctionPipe>() }
	}

	sealed interface LeakablePipe {
		val leakRate: Double
	}

	class RegularLinearPipe(override val location: BlockKey, val axis: Axis) : FluidNode(5.0), LeakablePipe {
		override val flowCapacity: Double = 5.0

		override val leakRate: Double = 1.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_PIPE
		}

		override fun getPipableDirections(): Set<BlockFace> = setOf(axis.faces.first, axis.faces.second)

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<RegularLinearPipe>() }
	}

	class Input(override val location: BlockKey) : FluidNode(0.0) {
		override val flowCapacity = 50.0

		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.blockData.customBlock?.key == CustomBlockKeys.FLUID_INPUT
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<Input>() }
	}
}
