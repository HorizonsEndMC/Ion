package net.horizonsend.ion.server.features.transport.node

import org.bukkit.block.BlockFace

/**
 * This class represents a relationship between two nodes
 * The information contains whether they may transfer to / from each other, from each side
 **/
data class NodeRelationship(
	val holder: TransportNode,
	val other: TransportNode,
	val offset: BlockFace,
	val canTransfer: Boolean
) {
	/**
	 * Break the relation between the two nodes
	 **/
	fun breakUp() {
		holder.removeRelationship(other)
		other.removeRelationship(holder)
		holder.refreshTransferCache()
		other.refreshTransferCache()
	}
}


