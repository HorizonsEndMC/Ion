package net.horizonsend.ion.server.features.transport.nodes.graph

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

abstract class TransportNode(val location: BlockKey, val type: TransportNodeType<*>) {
	fun getCenter() = toVec3i(location).toCenterVector()

	/**
	 * Returns if it is intact, null if it cannot be determined
	 **/
	abstract fun isIntact(): Boolean?

	abstract fun setNetworkOwner(graph: TransportNetwork<*>)
	abstract fun getNetwork(): TransportNetwork<*>

	abstract fun getPipableDirections(): Set<BlockFace>

	open fun onLoadedIntoNetwork(network: TransportNetwork<*>) {}

	companion object {
		val NODE_POSITION = NamespacedKeys.key("node_position")
	}

	protected fun getBlock(): Block? {
		val world = getNetwork().manager.transportManager.getWorld()
		val globalVec3i = getNetwork().manager.transportManager.getGlobalCoordinate(toVec3i(location))
		return getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null
	}
}
