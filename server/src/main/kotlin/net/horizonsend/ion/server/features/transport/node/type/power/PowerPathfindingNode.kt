package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.transport.node.TransportNode

interface PowerPathfindingNode {
	/**
	 * For use in pathfinding. When entering from previous node, get the list of available next nodes. Mostly used by the flood fill.
	 **/
	fun getNextNodes(previous: TransportNode, destination: TransportNode?): ArrayDeque<TransportNode>
}
