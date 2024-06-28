package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.type.DestinationNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.MultiBranchHead
import net.horizonsend.ion.server.features.transport.step.head.SingleBranchHead

/** A result which moves the head of the branch forward, using the current node's pathfinding */
class MoveForward<T: ChunkTransportNetwork> : StepResult<T> {
	override suspend fun apply(headHolder: HeadHolder<T>) {
		val branchHead = headHolder.head
		println("Trying to move forward")

		if (branchHead is MultiBranchHead<*>) throw IllegalArgumentException("Multi branches can't be modified!")

		val currentNode = (branchHead as SingleBranchHead<T>).currentNode

		tryCast<StepHandler<T>>(currentNode) {
			println("was step handler")
			val next = getNextNode(headHolder.head) ?: return EndBranch<T>().apply(headHolder)
			println("next node was $next")

			branchHead.previousNodes.add(branchHead.currentNode)
			branchHead.currentNode = next
		}

		tryCast<DestinationNode<T>>(currentNode) {
			println("was destination")
			finishChain(headHolder.head)
		}

		if (branchHead is SourceNode<*>) throw NotImplementedError()
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
