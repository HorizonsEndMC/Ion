package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.transport.node.TransportNode
import java.util.concurrent.atomic.AtomicInteger

interface Step {
	val steps: AtomicInteger
	val currentNode: TransportNode
	val share: Float
	val traversedNodes: MutableList<TransportNode>

	suspend operator fun invoke() {
		if (steps.incrementAndGet() > MAX_DEPTH) return

		traversedNodes.add(currentNode)
		currentNode.handleStep(this)

//		println("""
//			Step has been invoked
//			$steps steps taken
//			currently on $currentNode
//			has $share share
//			traversed through $traversedNodes
//		""".trimIndent())
	}

	companion object {
		const val MAX_DEPTH = 200
	}
}

data class PowerOriginStep(
	override val steps: AtomicInteger,
	override val currentNode: TransportNode,
	var power: Int,
	override val share: Float = 1f
) : Step {
	override val traversedNodes: MutableList<TransportNode> = mutableListOf()
}

data class TransportStep(
	val origin: PowerOriginStep,
	override val steps: AtomicInteger,
	override val currentNode: TransportNode,
	val previous: Step,
	override val share: Float = 1f
) : Step {
	override val traversedNodes: MutableList<TransportNode> = mutableListOf()
}
