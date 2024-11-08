package net.horizonsend.ion.server.features.transport.cache

import org.bukkit.Material
import org.bukkit.block.BlockFace

interface CachedNode {
	// The backing material(s) of this node type
	fun isMatchingMaterial(material: Material): Boolean

	/**
	 * Gets the directions that should be checked for the next nodes
	 **/
	fun getNextNodes(inputDirection: BlockFace): Set<BlockFace>
}
