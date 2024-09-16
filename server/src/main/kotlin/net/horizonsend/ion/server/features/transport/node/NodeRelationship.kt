package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
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

	companion object {
		fun create(point: BlockKey, holder: TransportNode, other: TransportNode, nodeTwoOffset: BlockFace) {
			val holderToOther = holder.isTransferableTo(other)
			val otherToHolder = other.isTransferableTo(holder)

			holder.relationships[point] = NodeRelationship(holder, other, nodeTwoOffset, holderToOther)
			other.relationships[point] = NodeRelationship(other, holder, nodeTwoOffset.oppositeFace, otherToHolder)
			holder.refreshTransferCache()
			other.refreshTransferCache()
		}
	}
}


