package net.horizonsend.ion.server.features.transport.step.head.gas

import net.horizonsend.ion.server.features.transport.network.TransportNetwork as ChunkTransportNetwork1
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.DestinationNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.SingleBranchHead
import org.bukkit.block.BlockFace

/**
 * Transferred power down a single path.
 *
 *
 **/
class SingleGasBranchHead(
	override val holder: HeadHolder<PowerNetwork>,
	override var lastDirection: BlockFace,
	override var currentNode: TransportNode,
	override val share: Double,
	override val previousNodes: MutableSet<TransportNode> = mutableSetOf()
) : SingleBranchHead<PowerNetwork>, GasBranchHead {
	private var isDead = false

	override fun markDead() {
		isDead = true
	}

	override fun isDead(): Boolean = isDead

	@Suppress("UNCHECKED_CAST")
	override suspend fun stepForward() {
		val node = currentNode

		if (tryCast<DestinationNode<PowerNetwork>>(node) { finishChain(this@SingleGasBranchHead) }) return

		// All other nodes handle steps transferring in / out
		node as StepHandler<ChunkTransportNetwork1>

		val result = node.handleHeadStep(this as SingleBranchHead<TransportNetwork>)

		result.apply(holder as HeadHolder<TransportNetwork>)
	}

	// Get around runtime type erasure
	private inline fun <reified T> tryCast(instance: Any?, block: T.() -> Unit): Boolean {
		if (instance is T) {
			block(instance)
			return true
		}

		return false
	}
}
