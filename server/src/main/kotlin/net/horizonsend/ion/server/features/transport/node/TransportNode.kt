package net.horizonsend.ion.server.features.transport.node

/**
 * Represents a single node, or step, in a transport grid
 *
 * A node may cover many blocks to avoid making unnecessary steps
 **/
interface TransportNode {
	/**
	 * The positions occupied by the node
	 **/
	val positions: MutableSet<Long>

	/**
	 * The neighboring nodes that this node may transport to
	 **/
	val transferableNeighbors: MutableSet<TransportNode>

	/**
	 * Returns whether this node may transport to the provided node
	 **/
	fun isTransferable(position: Long, node: TransportNode): Boolean


	// Section Utility

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	fun drainTo(new: TransportNode, nodes: MutableMap<Long, TransportNode>) {
		new.positions.addAll(positions)
		new.transferableNeighbors.addAll(transferableNeighbors)

		for (position in positions) {
			nodes[position] = new
		}
	}
}
