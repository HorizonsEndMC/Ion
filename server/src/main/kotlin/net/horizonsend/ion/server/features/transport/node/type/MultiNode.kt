package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
interface MultiNode<Self: MultiNode<Self, Z>, Z: MultiNode<Z, Self>> : TransportNode {
	/** The positions occupied by the node **/
	val positions: MutableSet<BlockKey>

	/**
	 * Rebuild the node during the removal process
	 *
	 * When a position in a multi node is removed, the removed position is removed
	 * from the list of contained positions, and the node is rebuilt using this method.
	 **/
	suspend fun rebuildNode(position: BlockKey)

	/**
	 * Adds new a position to this node
	 **/
	suspend fun addPosition(position: BlockKey): Self {
		positions += position
		network.nodes[position] = this

		onPlace(position)

		@Suppress("UNCHECKED_CAST")
		return this as Self
	}

	/**
	 * Adds multiple positions to this node
	 **/
	suspend fun addPositions(newPositions: Iterable<BlockKey>) {
		for (position in newPositions) {
			positions += position
			network.nodes[position] = this

			onPlace(position)
		}
	}

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	suspend fun drainTo(new: Self) {
		clearRelations()
		new.clearRelations()

		new.addPositions(positions)
		new.positions.forEach { new.buildRelations(it) }
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.getNode(offsetKey) ?: continue

			if (this == neighborNode) continue

			addRelationship(neighborNode)
		}
	}

	suspend fun rebuildRelations() {
		clearRelations()

		positions.forEach {
			buildRelations(it)
		}
	}

	override suspend fun handleRemoval(position: BlockKey) {
		isDead = true

		// Remove the position from the network
		network.nodes.remove(position)
		// Remove the position from this node
		positions.remove(position)

		// Remove all positions
		positions.forEach {
			network.nodes.remove(it)
		}

		// Rebuild relations after cleared
		clearRelations()

		// Rebuild the node without the lost position
		rebuildNode(position)
	}

	override suspend fun onPlace(position: BlockKey) {}

	override fun loadIntoNetwork() {
		for (key in positions) {
			network.nodes[key] = this
		}
	}
}
