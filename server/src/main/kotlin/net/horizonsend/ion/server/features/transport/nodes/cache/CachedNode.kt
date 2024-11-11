package net.horizonsend.ion.server.features.transport.nodes.cache

import org.bukkit.Material
import org.bukkit.block.BlockFace

interface CachedNode {
	val pathfindingResistance: Double
	/**
	 * Gets the directions that should be checked for the next nodes
	 *
	 * Returns a map of block face to transfer priority
	 **/
	fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>>

	/**
	 * Adds any restrictions on transferring to another node
	 **/
	fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean

	/**
	 * Adds any restrictions on transferring from another node
	 **/
	fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean
}
