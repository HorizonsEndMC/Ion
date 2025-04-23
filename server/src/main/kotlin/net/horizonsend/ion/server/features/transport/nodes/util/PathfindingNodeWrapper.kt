package net.horizonsend.ion.server.features.transport.nodes.util

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.types.TrackedNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

/**
 * @param node The cached node at this position
 * @param parent The parent node
 **/
class PathfindingNodeWrapper private constructor(val node: NodePositionData, parent: PathfindingNodeWrapper?, val interestingNodeLookup: ObjectOpenHashSet<Pair<BlockKey, Node>>) : Comparable<PathfindingNodeWrapper> {
	init {
	    if (node.type is TrackedNode) {
			interestingNodeLookup.add(node.position to node.type)
		}
	}

	companion object {
		fun newPath(node: NodePositionData): PathfindingNodeWrapper {
			return PathfindingNodeWrapper(node, null, ObjectOpenHashSet())
		}

		fun fromParent(node: NodePositionData, parent: PathfindingNodeWrapper): PathfindingNodeWrapper {
			return PathfindingNodeWrapper(node, parent, parent.interestingNodeLookup)
		}
	}

	private var depth: Int = (parent?.depth ?: 0) + 1

	var parent: PathfindingNodeWrapper? = parent
		set(value) {
			field = value
			depth = (value?.depth ?: 0) + 1
		}

	/**
	 * @param retainfull If true, build a full path of every node pathed over, if false the list will only contain tracked nodes.
	 **/
	fun buildPath(retainfull: Boolean): Path {
		// No tracked nodes on path, no need to compute the whole thing if the full path isn't requested
		if (!retainfull) {
			return Path(depth, interestingNodeLookup.toTypedArray())
		}

		val list = arrayListOf(this.node.position to this.node.type)
		var current: PathfindingNodeWrapper? = this

		// Iterate through each node's parents
		while (current?.parent != null) {
			current = current.parent!!

			if (current.node.type is TrackedNode) {
				list.add(current.node.position to current.node.type)
			}
		}

		return Path(depth, list.toTypedArray())
	}

	override fun compareTo(other: PathfindingNodeWrapper): Int {
		return depth.compareTo(other.depth)
	}
}
