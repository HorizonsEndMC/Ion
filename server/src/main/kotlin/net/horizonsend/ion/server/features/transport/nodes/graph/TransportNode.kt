package net.horizonsend.ion.server.features.transport.nodes.graph

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.block.BlockFace

interface TransportNode {
	val location: BlockKey
	val type: TransportNodeType<*>

	fun getCenter() = toVec3i(location).toCenterVector()

	/**
	 * Returns if it is intact, null if it cannot be determined
	 **/
	fun isIntact(): Boolean?

	fun setNetworkOwner(graph: TransportNetwork<*>)
	fun getNetwork(): TransportNetwork<*>

	fun getPipableDirections(): Set<BlockFace>

	fun onLoadedIntoNetwork(network: TransportNetwork<*>) {}

	companion object {
		val NODE_POSITION = NamespacedKeys.key("node_position")
	}
}
