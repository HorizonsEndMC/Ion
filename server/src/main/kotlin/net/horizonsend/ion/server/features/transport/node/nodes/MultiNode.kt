package net.horizonsend.ion.server.features.transport.node.nodes

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
interface MultiNode : TransportNode {
	/**
	 * The positions occupied by the node
	 **/
	val positions: MutableSet<Long>

	/**
	 * Returns whether the removal of the provided position should result in the splitting of a combined node
	 **/
	fun shouldSplit(position: Long, nodes: MutableMap<Long, TransportNode>): Boolean

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	fun drainTo(new: MultiNode, nodes: MutableMap<Long, TransportNode>) {
		new.positions.addAll(positions)
		new.transferableNeighbors.addAll(transferableNeighbors)

		for (position in positions) {
			nodes[position] = new
		}
	}
}
