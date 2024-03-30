package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.features.transport.node.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.features.transport.step.Step
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

class MergeNode(
	override val parentGrid: Grid,
	override val x: Int,
	override val y: Int,
	override val z: Int,
) : GridNode {
	override val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()

	override fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean {
		return node !is ExtractorNode<*>
	}

	override fun processStep(step: Step) {
		TODO("Not yet implemented")
	}
}
