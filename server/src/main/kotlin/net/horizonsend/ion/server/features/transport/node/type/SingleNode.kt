package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import kotlin.properties.Delegates

/**
 * A node that only occupies a single block
 **/
abstract class SingleNode : TransportNode() {
	var position by Delegates.notNull<Long>()

	override fun loadIntoNetwork() {
		manager.nodes[position] = this
	}

	override fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = manager.getNode(offsetKey) ?: continue

			if (this == neighborNode) return

			// Add a relationship, if one should be added
			addRelationship(position, neighborNode, offset)
		}
	}

	override fun onPlace(position: BlockKey) {
		buildRelations(position)
	}

	override fun handlePositionRemoval(position: BlockKey) {
		isDead = true
		manager.nodes.remove(position)
		clearRelations()
	}

	override fun getCenter(): Vec3i {
		return toVec3i(position)
	}
}
