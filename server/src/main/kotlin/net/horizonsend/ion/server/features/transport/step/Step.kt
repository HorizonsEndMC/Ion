package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.transport.node.TransportNode
import java.util.concurrent.atomic.AtomicInteger

interface Step {
	val steps: AtomicInteger
	val currentNode: TransportNode
	val share: Float

	suspend operator fun invoke() {
		if (steps.incrementAndGet() > MAX_DEPTH) return

		currentNode.handleStep(this)
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
) : Step

data class TransportStep(
	val origin: PowerOriginStep,
	override val steps: AtomicInteger,
	override val currentNode: TransportNode,
	val previous: Step,
	override val share: Float = 1f
) : Step
