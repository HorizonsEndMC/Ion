package net.horizonsend.ion.server.features.transport.grid.node

import net.horizonsend.ion.server.features.transport.grid.AbstractGrid
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_PAIRS
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a straight connection, e.g. wire, glass pane
 *
 * @param positions The keys of the positions that this straight node occupies
 **/
class StraightNode(
	override val parent: AbstractGrid,
	override val x: Int,
	override val y: Int,
	override val z: Int,
	override val neighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap(),
	val positions: Set<Long>
) : GridNode {
	override fun consolidate() {
		// Check neighbors in straight line
		for (adjacentPairs in ADJACENT_PAIRS) {
			if (!adjacentPairs.all {
				getNeighbor(it) is StraightNode
			}) continue

			val neighborNodes = adjacentPairs.mapNotNull(::getNeighbor)

			// Keys of all 3
			val occupiedKeys = neighborNodes.mapTo(mutableSetOf()) { it.key }.plus(key)

			// Add all the merged neighbors together
			val newNeighborsList = ConcurrentHashMap<BlockFace, GridNode>()
			neighborNodes.mapTo(mutableListOf()) { it.neighbors }.plus(neighbors).forEach(newNeighborsList::putAll)

			// Create a new node representing a single straight connection between all
			val new = StraightNode(
				parent,
				x, y, z,
				newNeighborsList,
				occupiedKeys
			)

			// Replace the neighbor positions with the new straight one and update neighbor relations
			neighborNodes.forEach { node ->
				val key = node.key

				parent.nodes[key] = new

				node.replace(new)
			}

			// Replace this one
			parent.nodes[key] = new
			replace(new)

			// Only merge once
			return
		}
	}
}
