package net.horizonsend.ion.server.features.transport.node.type

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.GridType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
abstract class MultiNode<Self: MultiNode<Self, Z>, Z: MultiNode<Z, Self>>(type: GridType) : TransportNode(type) {
	/** The positions occupied by the node **/
	val positions: MutableSet<BlockKey> = LongOpenHashSet()

	/**
	 * Rebuild the node during the removal process
	 *
	 * When a position in a multi node is removed, the removed position is removed
	 * from the list of contained positions, and the node is rebuilt using this method.
	 **/
	open suspend fun rebuildNode(position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			addBack(it)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			manager.nodes[it]?.buildRelations(it)
		}

		// Join successor nodes to the grid
		positions
			.mapNotNullTo(mutableSetOf()) { manager.nodes[it] }
			.forEach { it.joinGrid() }
	}

	abstract suspend fun addBack(position: BlockKey)

	/**
	 * Adds new a position to this node
	 **/
	suspend fun addPosition(position: BlockKey): Self {
		positions += position
		manager.nodes[position] = this

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
			manager.nodes[position] = this

			onPlace(position)
		}
	}

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	suspend fun drainTo(new: Self) {
		clearRelations()
		new.clearRelations()

		grid.removeNode(this)

		new.addPositions(positions)
		new.positions.forEach { new.buildRelations(it) }
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = manager.getNode(offsetKey) ?: continue

			if (this == neighborNode) continue

			addRelationship(neighborNode, offset)
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

		grid.removeNode(this)

		// Remove the position from the network
		manager.nodes.remove(position)
		// Remove the position from this node
		positions.remove(position)

		// Remove all positions
		positions.forEach {
			manager.nodes.remove(it)
		}

		// Rebuild relations after cleared
		clearRelations()

		// Rebuild the node without the lost position
		rebuildNode(position)
	}

	override fun loadIntoNetwork() {
		for (key in positions) {
			manager.nodes[key] = this
		}
	}
}
