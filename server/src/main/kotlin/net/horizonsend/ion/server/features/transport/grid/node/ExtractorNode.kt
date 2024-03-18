package net.horizonsend.ion.server.features.transport.grid.node

import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

abstract class ExtractorNode(
	override val x: Int,
	override val y: Int,
	override val z: Int,
	override val neighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()
) : GridNode {

}
