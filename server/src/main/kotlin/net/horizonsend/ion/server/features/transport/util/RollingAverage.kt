package net.horizonsend.ion.server.features.transport.util

import java.util.concurrent.TimeUnit

class RollingAverage {
	/** Data entry of transferred power, contains the amount and the timestamp if the transfer */
	data class TransferredPower(val transferred: Int, val time: Long = System.currentTimeMillis())

	companion object {
		private val DURATION_STORED_AVERAGES = TimeUnit.SECONDS.toMillis(5)
	}

	// Use array deque as a stack
	private val averages = ArrayDeque<TransferredPower>()

	@Synchronized
	fun addEntry(amount: Int) {
		val now = System.currentTimeMillis()

		val iterator = averages.iterator()
		while (iterator.hasNext()) {
			val entry = iterator.next()
			if (now - entry.time < DURATION_STORED_AVERAGES) break
			iterator.remove()
		}

		averages.addLast(TransferredPower(amount))
	}

	fun getAverage(): Double {
		if (averages.isEmpty()) {
			return 0.0
		}

		val sum = averages.sumOf { it.transferred }
		val timeDiff = maxOf(System.currentTimeMillis() - averages.first().time, 1) / 1000.0

		val rate = sum / timeDiff
		return rate
	}
}
