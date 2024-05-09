package net.horizonsend.ion.server.features.transport.node

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
	 **/
	data class RelationSide(val node: TransportNode, val transferAllowed: Boolean)

	/**
	 * Break the relation between the two nodes
	 **/
	fun breakUp() {
		sideOne.node.relationships.remove(this)
		sideTwo.node.relationships.remove(this)
	}

	/**
	 * If by some occurrence a node has entered into a relationship with itself, remove that relation
	 **/
	fun removeSelfRelation() {
		if (sideOne.node == sideTwo.node) {
			breakUp()
		}
	}

	/**
	 * Replace one side of the relationship with a new one
	 *
	 * This will clear relations to the previous node and add relations to the new one
	 **/
	fun replaceSide(replacedSide: TransportNode, newPartner: TransportNode) {
		breakUp()

		// Find which side is being replaced
		when (replacedSide) {
			sideOne.node -> {
				val new = create(newPartner, sideTwo.node) ?: return
				sideTwo.node.relationships.add(new)
			}
			sideTwo.node -> {
				val new = create(newPartner, sideOne.node) ?: return
				sideOne.node.relationships.add(new)
			}
			else -> throw IllegalArgumentException("Cannot replace node that is not present in relationship")
		}
	}

	companion object {
		fun create(nodeOne: TransportNode, nodeTwo: TransportNode): NodeRelationship? {
			val canTransferTo = nodeOne.isTransferableTo(nodeTwo)
			val canTransferFrom = nodeTwo.isTransferableTo(nodeOne)

			// Do not add the relationship if neither side can transfer
			if (!canTransferFrom && !canTransferTo) return null

			return NodeRelationship(RelationSide(nodeOne, canTransferTo), RelationSide(nodeTwo, canTransferFrom),)
		}
	}
}


