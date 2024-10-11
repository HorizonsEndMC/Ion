package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace

/**
 * This class represents a relationship between two nodes
 * The information contains whether they may transfer to / from each other, from each side
 **/
data class NodeRelationship(
	val position: BlockKey,
	val otherPosition: BlockKey,
	val holder: TransportNode,
	val other: TransportNode,
	val offset: BlockFace,
	val canTransfer: Boolean
) {
	/**
	 * Break the relation between the two nodes
	 **/
	fun breakUp() {
		holder.relationHolder.raw()[position]?.remove(this)
		other.relationHolder.raw()[otherPosition]?.remove(this)
		holder.refreshTransferCache()
		other.refreshTransferCache()
	}
}


