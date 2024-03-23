package net.horizonsend.ion.server.features.transport.grid.node

import net.horizonsend.ion.server.features.transport.grid.Grid
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

/**
 * An omnidirectional node
 **/
class JunctionNode(
	override val parentGrid: Grid,
	override val x: Int,
	override val y: Int,
	override val z: Int,
	override val neighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()
) : GridNode
