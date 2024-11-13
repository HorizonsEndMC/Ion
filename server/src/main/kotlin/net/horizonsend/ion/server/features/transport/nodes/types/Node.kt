package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getOrCacheNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.World
import org.bukkit.block.BlockFace

interface Node {
	val cacheType: CacheType
	val pathfindingResistance: Double

	fun getTransferableDirections(backwards: BlockFace): Set<BlockFace>

	/**
	 * Adds any restrictions on transferring to another node
	 **/
	fun canTransferTo(other: Node, offset: BlockFace): Boolean

	/**
	 * Adds any restrictions on transferring from another node
	 **/
	fun canTransferFrom(other: Node, offset: BlockFace): Boolean

	fun getNextNodes(world: World, position: BlockKey, backwards: BlockFace): List<NodePositionData> {
		val adjacent = getTransferableDirections(backwards)
		val nodes = mutableListOf<NodePositionData>()

		for (adjacentFace in adjacent) {
			val pos = getRelative(position, adjacentFace)
			val cached = getOrCacheNode(cacheType, world, pos) ?: continue

			if (!cached.canTransferFrom(this, adjacentFace) || !canTransferTo(cached, adjacentFace)) continue

			nodes.add(NodePositionData(cached, world, getRelative(position, adjacentFace), adjacentFace))
		}

		return nodes
	}

	data class NodePositionData(val type: Node, val world: World, val position: BlockKey, val offset: BlockFace) {
		fun getNextNodes(): List<NodePositionData> = type.getNextNodes(world, position, offset.oppositeFace)
	}

	fun onInvalidate() {}
}
