package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.popMaxByOrNull
import org.bukkit.World
import org.bukkit.block.BlockFace

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
suspend fun <Self: MultiNode<Self, Self>> handleMerges(neighbors: MutableCollection<Self>): Self {
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

fun getNode(world: World, key: BlockKey, networkType: NetworkType): TransportNode? {
	val x = getX(key).shr(4)
	val z = getZ(key).shr(4)

	val chunk = IonChunk[world, x, z] ?: return null
	return networkType.get(chunk).nodes[key]
}

enum class NetworkType {
	POWER {
		override fun get(chunk: IonChunk): NodeManager {
			return chunk.transportNetwork.powerNodeManager.network
		}

		override fun get(ship: ActiveStarship): NodeManager {
			TODO("Not yet implemented")
		}
	},
	FLUID {
		override fun get(chunk: IonChunk): NodeManager {
			return chunk.transportNetwork.powerNodeManager.network
		}

		override fun get(ship: ActiveStarship): NodeManager {
			TODO("Not yet implemented")
		}
	},


	;

	abstract fun get(chunk: IonChunk): NodeManager
	abstract fun get(ship: ActiveStarship): NodeManager
}
