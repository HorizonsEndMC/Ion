package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.server.features.transport.manager.holders.CacheProvider
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
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

	/**
	 * Gets the max number of times this node can be iterated over during pathfinding
	 **/
	fun getMaxPathfinds(): Int = 1

	fun getNextNodes(
		currentCache: TransportCache,
		world: World,
		position: BlockKey,
		backwards: BlockFace,
		cachedNodeProvider: CacheProvider,
		filter: ((Node, BlockFace) -> Boolean)?
	): List<NodePositionData> {
		val adjacent = getTransferableDirections(backwards)
		val nodes = mutableListOf<NodePositionData>()

		for (adjacentFace in adjacent) {
			val relativePos = getRelative(position, adjacentFace)
			val cacheResult = cachedNodeProvider.invoke(currentCache, cacheType, world, relativePos) ?: continue
			val (cache, cached) = cacheResult
			if (cached == null) continue

			if (!cached.canTransferFrom(this, adjacentFace) || !canTransferTo(cached, adjacentFace)) continue
			if (filter != null && !filter.invoke(cached, adjacentFace)) continue

			nodes.add(NodePositionData(
				type = cached,
				world = world,
				position = relativePos,
				offset = adjacentFace,
				cache = cache
			))
		}

		return filterPositionData(nodes, backwards)
	}

	/** Altered node provider, designed for traversing networks backwards */
	fun getPreviousNodes(
		positionData: NodePositionData,
		cachedNodeProvider: CacheProvider,
		filter: ((Node, BlockFace) -> Boolean)?
	): List<NodePositionData> {
		val backwards = positionData.offset.oppositeFace
		val nodes = mutableListOf<NodePositionData>()

		for (adjacentFace in ADJACENT_BLOCK_FACES) {
			val relativePos = getRelative(positionData.position, adjacentFace)
			val cacheResult = cachedNodeProvider.invoke(positionData.cache, cacheType, positionData.world, relativePos) ?: continue
			val (cache, cached) = cacheResult
			if (cached == null) continue

			if (!cached.canTransferTo(this, adjacentFace.oppositeFace) || !canTransferFrom(cached, adjacentFace.oppositeFace)) continue
			if (filter != null && !filter.invoke(cached, adjacentFace.oppositeFace)) continue

			if (cached.filterPositionData(nextNodes = listOf(positionData), backwards = adjacentFace.oppositeFace).isEmpty()) continue

			nodes.add(NodePositionData(
				type = cached,
				world = positionData.world,
				position = relativePos,
				offset = adjacentFace.oppositeFace,
				cache = cache,
			))
		}

		return nodes
	}

	/**
	 * Filters the found adjacent nodes, after checking for transport possibility
	 **/
	fun filterPositionData(nextNodes: List<NodePositionData>, backwards: BlockFace): List<NodePositionData> = nextNodes
	fun filterPositionDataBackwards(previousNodes: List<NodePositionData>, backwards: BlockFace): List<NodePositionData> = previousNodes

	data class NodePositionData(val type: Node, val world: World, val position: BlockKey, val offset: BlockFace, val cache: TransportCache) {
		fun getNextNodes(cachedNodeProvider: CacheProvider, filter: ((Node, BlockFace) -> Boolean)?): List<NodePositionData> =
				type.getNextNodes(cache, world, position, offset.oppositeFace, cachedNodeProvider, filter)

		fun getPreviousNodes(cachedNodeProvider: CacheProvider, filter: ((Node, BlockFace) -> Boolean)?): List<NodePositionData> =
				type.getPreviousNodes(this, cachedNodeProvider, filter)
	}

	fun onInvalidate() {}
}
