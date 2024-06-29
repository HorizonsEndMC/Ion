package net.horizonsend.ion.server.features.transport.step.head.power

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork as ChunkTransportNetwork1
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.DestinationNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.SingleBranchHead

/**
 * Transferred power down a single path.
 *
 *
 **/
class SinglePowerBranchHead(
	override val holder: Step<ChunkPowerNetwork>,
	override var currentNode: TransportNode,
	override val share: Double,
	override val previousNodes: MutableSet<TransportNode> = mutableSetOf()
) : SingleBranchHead<ChunkPowerNetwork>, PowerBranchHead {
	private var isDead = false

	override fun markDead() {
		isDead = true
	}

	override fun isDead(): Boolean = isDead

	@Suppress("UNCHECKED_CAST")
	override suspend fun stepForward() {
		val node = currentNode

		if (tryCast<DestinationNode<ChunkPowerNetwork>>(node) { finishChain(this@SinglePowerBranchHead) }) return

		// All other nodes handle steps transferring in / out
		node as StepHandler<ChunkTransportNetwork1>

		val result = node.handleHeadStep(this as BranchHead<ChunkTransportNetwork>)

		result.apply(holder as HeadHolder<ChunkTransportNetwork>)
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
