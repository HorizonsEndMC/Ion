package net.horizonsend.ion.server.features.transport.node.nodes

/**
 * Represents a single node, or step, in a transport grid
 **/
interface TransportNode {

	/**
	 * The neighboring nodes that this node may transport to
	 **/
	val transferableNeighbors: MutableSet<TransportNode>

	/**
	 * Returns whether this node may transport to the provided node
	 **/
	fun isTransferable(position: Long, node: TransportNode): Boolean
}
