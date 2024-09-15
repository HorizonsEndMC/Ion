package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.transport.node.separateNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import java.util.concurrent.ConcurrentHashMap

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
abstract class MultiNode<Self: MultiNode<Self, Z>, Z: MultiNode<Z, Self>> : TransportNode() {
	/** The positions occupied by the node **/
	val positions: MutableSet<BlockKey> = ConcurrentHashMap.newKeySet()

	/**
	 * Rebuild the node during the removal process
	 *
	 * When a position in a multi node is removed, the removed position is removed
	 * from the list of contained positions, and the node is rebuilt using this method.
	 **/
	open fun rebuildNode(position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			addBack(it)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			manager.nodes[it]?.buildRelations(it)
		}
	}

	/**
	 * Adds new a position to this node
	 **/
	fun addPosition(position: BlockKey): Self {
		positions += position
		manager.nodes[position] = this

		onPlace(position)

		@Suppress("UNCHECKED_CAST")
		return this as Self
	}

	/**
	 * Adds multiple positions to this node
	 **/
	fun addPositions(newPositions: Iterable<BlockKey>) {
		for (position in newPositions) {
			positions += position
			manager.nodes[position] = this

			onPlace(position)
		}
	}

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	 fun drainTo(new: Self) {
		clearRelations()
		new.clearRelations()

		new.addPositions(positions)
		new.positions.forEach { new.buildRelations(it) }
	}

	override fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = manager.getNode(offsetKey) ?: continue

			if (this == neighborNode) continue

			addRelationship(position, neighborNode, offset)
		}
	}

	fun rebuildRelations() {
		clearRelations()

		positions.forEach {
			buildRelations(it)
		}
	}

	/**
	 * Handle the removal of a position. Slits the node if necessary
	 *
	 **/
	override fun handlePositionRemoval(position: BlockKey) {
		// Remove the position from the network
		manager.nodes.remove(position)

		// Remove the position from this node
		positions.remove(position)
		removeRelationship(position)

		if (separateNode(this)) {
			positions.clear()
			clearRelations()
		}
	}

	override fun loadIntoNetwork() {
		for (key in positions) {
			manager.nodes[key] = this
		}
	}

	/**
	 * Returns the directly adjacent positions to this position that this node contains
	 **/
	fun adjacentPositions(key: BlockKey): Set<BlockKey> {
		return ADJACENT_BLOCK_FACES.mapNotNullTo(mutableSetOf()) { face ->
			getRelative(key, face).takeIf { positions.contains(it) }
		}
	}

	/**
	 * Places the set of positions into the network as this type of node.
	 **/
	fun ofPositions(positions: Set<BlockKey>) {
		@Suppress("UNCHECKED_CAST")
		val newNode = type.newInstance(manager) as Self

		positions.forEach {
			newNode.addPosition(it)
			newNode.buildRelations(it)
		}
	}

	abstract fun addBack(position: BlockKey)

	open val maxPositions: Int = 32

	open fun canAdd(position: BlockKey): Boolean {
		val holder = manager.holder as? ChunkNetworkHolder<*>
		if (holder != null &&
			(getX(position).shr(4) != holder.manager.chunk.x ||
			getZ(position).shr(4) != holder.manager.chunk.z)
		) return false

		return positions.size < maxPositions
	}

	fun removeFromNetwork() {
		positions.forEach { manager.nodes.remove(it) }
	}
}
