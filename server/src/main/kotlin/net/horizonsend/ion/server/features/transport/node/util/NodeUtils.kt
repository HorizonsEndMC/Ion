package net.horizonsend.ion.server.features.transport.node.util

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.cache.TransportCache
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.popMaxByOrNull
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import java.util.LinkedList

fun getNeighborNodes(position: BlockKey, nodes: Map<BlockKey, TransportNode>, checkFaces: Collection<BlockFace> = ADJACENT_BLOCK_FACES) = checkFaces.associateWithNotNull {
	val x = getX(position)
	val y = getY(position)
	val z = getZ(position)

	nodes[toBlockKey(x + it.modX, y + it.modY, z + it.modZ)]
}

fun getNeighborNodes(position: BlockKey, nodes: Collection<BlockKey>, checkFaces: Collection<BlockFace> = ADJACENT_BLOCK_FACES) = checkFaces.mapNotNull {
	val x = getX(position)
	val y = getY(position)
	val z = getZ(position)

	toBlockKey(x + it.modX, y + it.modY, z + it.modZ).takeIf { key -> nodes.contains(key) }
}

/**
 * Merge the provided nodes into the largest node [by position] in the collection
 *
 * @return the node that the provided were merged into
 **/
fun <Self: MultiNode<Self, Self>> handleMerges(neighbors: MutableCollection<Self>): Self {
	// Get the largest neighbor
	val largestNeighbor = neighbors.popMaxByOrNull {
		it.positions.size
	} ?: throw ConcurrentModificationException("Node removed during processing")

	// Merge all other connected nodes into the largest
	neighbors.forEach {
		it.drainTo(largestNeighbor)
	}

	return largestNeighbor
}

fun <G : MultiNode<*, *>> separateNode(node: G): Boolean {
	// Generate the grid nodes isolated from each other.
	val splitGraphs: List<Set<BlockKey>> = separateNodePositions(node)

	if (splitGraphs.size <= 1) return false

	// Create new nodes
	splitGraphs.forEach { node.ofPositions(it) }

	return true
}

/**
 * Splits a multi node's positions into multiple nodes
 * https://github.com/CoFH/ThermalDynamics/blob/1.20.x/src/main/java/cofh/thermal/dynamics/common/grid/GridContainer.java#L394
 **/
fun <T : MultiNode<*, *>> separateNodePositions(node: T): List<Set<BlockKey>> {
	val seen: MutableSet<BlockKey> = HashSet()
	val stack = LinkedList<BlockKey>()
	val separated: MutableList<Set<BlockKey>> = LinkedList()

	while (true) {
		var first: BlockKey? = null

		// Find next node in graph we haven't seen.
		for (position in node.positions) {
			if (!seen.contains(position)) {
				first = position
				break
			}
		}

		// We have discovered all nodes, exit.
		if (first == null) break

		// Start recursively building out all nodes in this sub-graph
		val subGraph: MutableSet<BlockKey> = HashSet()

		stack.push(first)

		while (!stack.isEmpty()) {
			val entry = stack.pop()

			if (seen.contains(entry)) continue

			stack.addAll(node.adjacentPositions(entry))
			seen.add(entry)
			subGraph.add(entry)
		}

		separated.add(subGraph)
	}

	return separated
}

enum class NetworkType(val namespacedKey: NamespacedKey) {
	POWER(NamespacedKeys.POWER_TRANSPORT) {
		override fun get(chunk: IonChunk): PowerTransportCache {
			return chunk.transportNetwork.powerNodeManager.network
		}

		override fun get(ship: ActiveStarship): PowerTransportCache {
			return ship.transportManager.powerNodeManager.network
		}
	},
	FLUID(NamespacedKeys.FLUID_TRANSPORT) {
		override fun get(chunk: IonChunk): FluidTransportCache {
			return chunk.transportNetwork.fluidNodeManager.network
		}

		override fun get(ship: ActiveStarship): FluidTransportCache {
			return ship.transportManager.fluidNodeManager.network
		}
	},


	;

	abstract fun get(chunk: IonChunk): TransportCache
	abstract fun get(ship: ActiveStarship): TransportCache

	companion object {
		private val byKey = entries.associateBy { it.namespacedKey }
		operator fun get(key: NamespacedKey): NetworkType = byKey[key]!!
	}
}
