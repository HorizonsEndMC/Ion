package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative

/**
 * A node that only occupies a single block
 **/
interface SingleNode : TransportNode {
	val position: Long

	override fun loadIntoNetwork() {
		network.nodes[position] = this
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.getNode(offsetKey) ?: continue

			if (this == neighborNode) return

			// Add a relationship, if one should be added
			addRelationship(neighborNode)
		}
	}

	override suspend fun onPlace(position: BlockKey) {
		buildRelations(position)
	}

	override suspend fun handleRemoval(position: BlockKey) {
		isDead = true
		network.nodes.remove(position)
		clearRelations()
	}
}
