package net.horizonsend.ion.server.features.transport.nodes.pathfinding

import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class PathTracker {
	/**
	 * Contains the positions that a path has been found on
	 **/
	private val coveredPositions = LongOpenHashSet()

	/**
	 * Contains a lookup from a node's position to an index in the path list
	 **/
	private val pathReferences = Long2ShortOpenHashMap()

	/**
	 * Contains the unique paths that have been found
	 **/
	private val paths = arrayListOf<Array<Node.NodePositionData>>()

	fun checkPosition(existingPath: PathfindingNodeWrapper): Array<Node.NodePositionData>? {
		val key = existingPath.node.position

		// If this position has not been traversed over in a prior path, then we cannot take a shortcut
		if (!coveredPositions.contains(key)) return null
		val pathIndex = pathReferences[key]

		val foundPath = paths[pathIndex.toInt()]
		val traversedNodes = existingPath.buildPath()

		// Build a new path from the origin to the join, and add the traversed nodes.
		val newPath = buildNewPath(key, foundPath, traversedNodes)

		// Add the newly created path to the lookup
		addPathLookup(newPath, traversedNodes)

		return newPath
	}

	private fun buildNewPath(branchPosition: BlockKey, foundPath: Array<Node.NodePositionData>, traversedNodes: Array<Node.NodePositionData>): Array<Node.NodePositionData> {
		val new = arrayListOf<Node.NodePositionData>()

		for (i in foundPath.indices) {
			val node = foundPath[i]
			new.add(node)

			if (node.position == branchPosition) break
		}

		// Add the newly built branch path to the trunk
		new.addAll(traversedNodes)

		return new.toTypedArray()
	}

	/**
	 * Adds provided path to the lookup
	 * @param fullPath The path to be added to the lookup
	 * @param addPositions The positions in which to reference this path
	 **/
	fun addPathLookup(fullPath: Array<Node.NodePositionData>, addPositions: Array<Node.NodePositionData>) {
		val newIndex = paths.size.toShort()
		paths.add(fullPath)
		for (node in addPositions) {
			coveredPositions.add(node.position)
			pathReferences[node.position] = newIndex
		}
	}
}
