package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.features.transport.step.Step
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

class PowerFlowMeter(
	override val parentTransportNetwork: ChunkTransportNetwork,
	override val x: Int,
	override val y: Int,
	override val z: Int,
) : GridNode, PowerNode {
	override val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()


	override fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean {
		return true
	}

	override fun processStep(step: Step) {
		TODO("Not yet implemented")
	}

	override val multiblocks: MutableList<PoweredMultiblockEntity>
		get() = TODO("Not yet implemented")
}
