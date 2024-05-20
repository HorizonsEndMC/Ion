package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.node.TransportNode
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.math.roundToInt

data class PowerExtractorOriginStep(
	override val steps: AtomicInteger,
	override val currentNode: TransportNode,
	val maxPower: Int,
	val extractable: List<PoweredMultiblockEntity>,
	override val traversedNodes: MutableSet<TransportNode> = mutableSetOf(),
) : OriginStep {
	override fun finishExtraction(final: TransportStep, amount: Int): Int {
		if (extractable.isEmpty()) return 0

		// Store multis that still have power
		val toExtract = extractable.toMutableList()

		// The share of the origin power along this branch of the chain
		val share = (final.share * maxPower).roundToInt()

		// The maximum that should be extracted
		// If limited, pick the lower
		val maxPower = min(amount, share)

		// Remaining power to take
		var remaining = maxPower

		// Take as much as possible
		while (remaining >= 0) {
			// Shouldn't be null but
			val nextMin = toExtract.minByOrNull { it.getPower() } ?: return 0

			// Take an equal amount from all multis so that the first is drained
			val minTake = min(remaining, nextMin.getPower() * toExtract.size)

			// Remove this amount from remaining
			remaining -= minTake

			// Perform the drain
			extractEqually(minTake)

			toExtract.remove(nextMin)
		}

		return maxPower - remaining
	}

	/**
	 * Extract an equal amount of power from the extractable power banks
	 **/
	private fun extractEqually(amount: Int) {
		val n = extractable.size

		extractable.forEach {
			it.removePower(amount / n)
		}
	}
}

data class SolarPowerOriginStep(
	override val steps: AtomicInteger,
	override val currentNode: TransportNode,
	val power: Int,
	override val traversedNodes: MutableSet<TransportNode> = mutableSetOf(),
) : OriginStep {
	// Solar panels do not have any additional logic needed upon extraction
	override fun finishExtraction(final: TransportStep, amount: Int): Int = amount
}

data class PowerTransportStep(
	val origin: OriginStep,
	override val steps: AtomicInteger,
	override val currentNode: TransportNode,
	val previous: Step,
	override val traversedNodes: MutableSet<TransportNode>,
	override val share: Float = 1f
) : TransportStep
