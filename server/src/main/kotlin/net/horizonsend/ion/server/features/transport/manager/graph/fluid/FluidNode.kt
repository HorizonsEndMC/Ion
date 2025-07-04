package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Material

abstract class FluidNode(val volume: Double) : TransportNode {
	private lateinit var graph: FluidNetwork

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as FluidNetwork
	}

	class RegularPipe(override val location: BlockKey) : FluidNode(10.0) {
		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.COPPER_GRATE || block.type == Material.WAXED_COPPER_GRATE
		}

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<RegularPipe>() }
	}

	class SraightPipe(override val location: BlockKey) : FluidNode(5.0) {
		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.LIGHTNING_ROD
		}

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<RegularPipe>() }
	}

	class Input(override val location: BlockKey) : FluidNode(0.0) {
		override fun isIntact(): Boolean? {
			val world = getNetwork().manager.transportManager.getWorld()
			val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.FLETCHING_TABLE
		}

		override fun getPersistentDataType(): TransportNode.NodePersistentDataType<*> = persistentDataType
		private companion object { val persistentDataType = TransportNode.NodePersistentDataType.simple<Input>() }
	}
}
