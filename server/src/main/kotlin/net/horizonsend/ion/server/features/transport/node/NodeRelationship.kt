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
		println("Removing ${sideOne.node}'s relation to ${sideTwo.node}")
		sideOne.node.removeRelationship(sideTwo.node)
		println("Removing ${sideTwo.node}'s relation to ${sideOne.node}")
		sideTwo.node.removeRelationship(sideOne.node)
	}

	companion object {
		fun create(nodeOne: TransportNode, nodeTwo: TransportNode) {
			println("Attempting to create relationship between $nodeOne and $nodeTwo")
			Throwable().printStackTrace()

			val canTransferTo = nodeOne.isTransferableTo(nodeTwo)
			val canTransferFrom = nodeTwo.isTransferableTo(nodeOne)

			println("Node one can transfer to node two: $canTransferTo")
			println("Node two can transfer to node one: $canTransferFrom")

			// Do not add the relationship if neither side can transfer
			if (!canTransferFrom && !canTransferTo) return

			println("Adding a relation between $nodeOne and $nodeTwo")
			nodeOne.relationships += NodeRelationship(RelationSide(nodeOne, canTransferTo), RelationSide(nodeTwo, canTransferFrom))
			println("Adding a relation between $nodeTwo and $nodeOne")
			nodeTwo.relationships += NodeRelationship(RelationSide(nodeTwo, canTransferFrom), RelationSide(nodeOne, canTransferTo))
		}
	}
}


