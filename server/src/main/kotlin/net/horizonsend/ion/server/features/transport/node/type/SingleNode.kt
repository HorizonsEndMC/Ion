package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.grid.GridType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import kotlin.properties.Delegates

/**
 * A node that only occupies a single block
 **/
abstract class SingleNode(type: GridType) : TransportNode(type) {
	var position by Delegates.notNull<Long>()

	override fun loadIntoNetwork() {
		manager.nodes[position] = this
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = manager.getNode(offsetKey) ?: continue

			if (this == neighborNode) return

			// Add a relationship, if one should be added
			addRelationship(neighborNode, offset)
		}
	}

	override suspend fun onPlace(position: BlockKey) {
		buildRelations(position)
		joinGrid()
	}

	override suspend fun handleRemoval(position: BlockKey) {
		isDead = true
		manager.nodes.remove(position)
		clearRelations()
		grid.removeNode(this)
	}
}
