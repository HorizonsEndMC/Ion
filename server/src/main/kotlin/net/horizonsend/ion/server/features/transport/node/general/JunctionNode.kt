package net.horizonsend.ion.server.features.transport.node.general

import net.horizonsend.ion.server.features.transport.grid.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.TransferStatus
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.asKotlinRandom

/**
 * An omnidirectional node
 **/
class JunctionNode(
	override val parentTransportNetwork: TransportNetwork,
	override val x: Int,
	override val y: Int,
	override val z: Int,
) : GridNode {
	override val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()

	override fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean {
		return node !is ExtractorNode<*>
	}

	override fun processStep(step: Step) {
		val neighbor = transferableNeighbors
			.entries
			.filter { it.key != step.direction }
			.randomOrNull(ThreadLocalRandom.current().asKotlinRandom())

		if (neighbor == null) {
			step.status = TransferStatus.BLOCKED

			return
		}

		val (offset, node) = neighbor

		step.direction = offset
		step.currentNode = node
	}
}
