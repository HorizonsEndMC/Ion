package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace

/**
 * This class represents a relationship between two nodes
 * The information contains whether they may transfer to / from each other, from each side
 **/
data class NodeRelationship(
	val sideOne: RelationSide,
	val sideTwo: RelationSide
) {
	/**
	 * A side on a node relationship
	 *
	 * @param node The node on this side of the relationship
	 * @param transferAllowed Whether this node is allowed to transfer to the other side
	 * @param nodeTwoOffset The BlockFace which this side can be found from the other side
	 **/
	data class RelationSide(val node: TransportNode, val transferAllowed: Boolean, val offset: BlockFace)

	/**
	 * Break the relation between the two nodes
	 **/
	fun breakUp() {
		sideOne.node.removeRelationship(sideTwo.node)
		sideTwo.node.removeRelationship(sideOne.node)
	}

	companion object {
		fun create(point: BlockKey, nodeOne: TransportNode, nodeTwo: TransportNode, nodeTwoOffset: BlockFace) {
			val canTransferTo = nodeOne.isTransferableTo(nodeTwo)
			val canTransferFrom = nodeTwo.isTransferableTo(nodeOne)

			// Do not add the relationship if neither side can transfer
			if (!canTransferFrom && !canTransferTo) return

			nodeOne.relationships[point] = NodeRelationship(RelationSide(nodeOne, canTransferTo, BlockFace.SELF), RelationSide(nodeTwo, canTransferFrom, nodeTwoOffset))
			nodeTwo.relationships[point] = NodeRelationship(RelationSide(nodeTwo, canTransferFrom, BlockFace.SELF), RelationSide(nodeOne, canTransferTo, nodeTwoOffset.oppositeFace))
		}
	}
}


