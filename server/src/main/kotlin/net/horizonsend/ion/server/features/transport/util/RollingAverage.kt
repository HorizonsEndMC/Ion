package net.horizonsend.ion.server.features.transport.util

class RollingAverage {
	/** Data entry of transferred power, contains the amount and the timestamp if the transfer */
	data class TransferredPower(val transferred: Int, val time: Long = System.currentTimeMillis())

	companion object { private const val NUMBER_STORED_AVERAGES = 20 }
	// Use array deque as a stack
	private val averages = ArrayDeque<TransferredPower?>(NUMBER_STORED_AVERAGES).apply {
		// Initialize with 0 transferred
		add(TransferredPower(0, System.currentTimeMillis()))
	}

	fun addEntry(amount: Int) {
		if (averages.size == NUMBER_STORED_AVERAGES) averages.removeFirst()
		averages.addLast(TransferredPower(amount))
	}

	fun getAverage(): Double {
		val nonNull = averages.filterNotNull()
		val sum = nonNull.sumOf { it.transferred }

		val timeDiff = (System.currentTimeMillis() - nonNull.minOf { it.time }) / 1000.0

		val rate = sum / timeDiff
		return rate
	}
}
