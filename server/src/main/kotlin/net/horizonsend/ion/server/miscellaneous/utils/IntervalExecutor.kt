package net.horizonsend.ion.server.miscellaneous.utils

/**
 * Only runs the attempted task once this has been called the set number of times
 **/
class IntervalExecutor(private val interval: Int, private val execute: () -> Unit) {
	var count: Int = 0;

	operator fun invoke() {
		if (count >= interval) {
			execute.invoke()
			count = 0
			return
		}

		count++
	}
}
