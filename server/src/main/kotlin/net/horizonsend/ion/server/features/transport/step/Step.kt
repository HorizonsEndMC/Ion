package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.transport.node.TransportNode
import java.util.concurrent.atomic.AtomicInteger

interface Step {
	val steps: AtomicInteger
	val currentNode: TransportNode
	val traversedNodes: MutableSet<TransportNode>

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

/**
 * A step that is the first in a chain
 **/
interface OriginStep : Step {
	/**
	 * Removes the appropriate amount of power. Returns the amount that was removed (and is available)
	 *
	 * @param final The final step of the chain
	 * @param amount The limit of extraction (will use either this value or the maximum amount defined in this config, whichever is lower)
	 **/
	fun finishExtraction(final: TransportStep, amount: Int): Int
}

interface TransportStep : Step {
	val share: Float
}
