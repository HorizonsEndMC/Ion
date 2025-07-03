package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Material
import kotlin.properties.Delegates

interface FluidNode : TransportNode {
	val volume: Double

	val graph: FluidNetwork

	class RegularPipe(override val location: BlockKey) : FluidNode {
		override var graph: FluidNetwork by Delegates.notNull<FluidNetwork>(); private set
		override val volume: Double = 10.0

		override fun isIntact(): Boolean? {
			val world = graph.manager.transportManager.getWorld()
			val globalVec3i = graph.manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.COPPER_GRATE
		}

		override fun setNetworkOwner(graph: TransportNetwork<*>) {
			this.graph = graph as FluidNetwork
		}

		override fun getGraph(): TransportNetwork<*> {
			return graph
		}

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<RegularPipe>() }
	}

	class SraightPipe(override val location: BlockKey) : FluidNode {
		override var graph: FluidNetwork by Delegates.notNull<FluidNetwork>(); private set
		override val volume: Double = 10.0

		override fun isIntact(): Boolean? {
			val world = graph.manager.transportManager.getWorld()
			val globalVec3i = graph.manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.LIGHTNING_ROD
		}

		override fun setNetworkOwner(graph: TransportNetwork<*>) {
			this.graph = graph as FluidNetwork
		}

		override fun getGraph(): TransportNetwork<*> {
			return graph
		}

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<RegularPipe>() }
	}

	class Input(override val location: BlockKey) : FluidNode {
		override var graph: FluidNetwork by Delegates.notNull<FluidNetwork>(); private set
		override val volume: Double = 10.0

		override fun isIntact(): Boolean? {
			val world = graph.manager.transportManager.getWorld()
			val globalVec3i = graph.manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.FLETCHING_TABLE
		}

		override fun setNetworkOwner(graph: TransportNetwork<*>) {
			this.graph = graph as FluidNetwork
		}

		override fun getGraph(): TransportNetwork<*> {
			return graph
		}

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<Input>() }
	}
}
