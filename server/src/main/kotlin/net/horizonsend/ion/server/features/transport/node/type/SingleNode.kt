package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
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
