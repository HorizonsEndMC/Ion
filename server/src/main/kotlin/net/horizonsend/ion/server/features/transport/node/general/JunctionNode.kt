package net.horizonsend.ion.server.features.transport.node.general

import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.features.transport.node.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.GridNode
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
	override val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()
) : GridNode {
	override fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean {
		return node !is ExtractorNode
	}
}
