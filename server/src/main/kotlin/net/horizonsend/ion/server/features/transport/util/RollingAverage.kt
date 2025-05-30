package net.horizonsend.ion.server.features.transport.util

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit

class RollingAverage {
	/** Data entry of transferred power, contains the amount and the timestamp if the transfer */
	data class TransferredPower(val transferred: Int, val time: Long = System.nanoTime())

	companion object {
		private val DURATION_STORED_AVERAGES = TimeUnit.SECONDS.toNanos(5)
		private val SECOND = TimeUnit.SECONDS.toNanos(1).toDouble()
	}

	// Use array deque as a stack
	private val averages = ConcurrentLinkedDeque<TransferredPower>()

	fun addEntry(amount: Int) {
		averages.addLast(TransferredPower(amount))
	}

	fun trimExpired() {
		val now = System.nanoTime()

		val iterator = averages.iterator()
		while (iterator.hasNext()) {
			val entry = iterator.next()
			val age = now - entry.time
			if (age < DURATION_STORED_AVERAGES) break
			iterator.remove()
		}
	}

	fun getAverage(): Double {
		trimExpired()

		if (averages.isEmpty()) {
			return 0.0
		}

		val sum = averages.sumOf { it.transferred }
		val timeDiff = maxOf(System.nanoTime() - averages.first().time, 1) / SECOND

		val rate = sum / timeDiff
		return rate
	}
}
