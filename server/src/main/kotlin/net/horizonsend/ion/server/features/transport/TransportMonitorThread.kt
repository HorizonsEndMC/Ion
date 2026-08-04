package net.horizonsend.ion.server.features.transport

import io.netty.util.internal.logging.Slf4JLoggerFactory
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

class TransportMonitorThread : Thread() {
	private val logger = Slf4JLoggerFactory.getInstance("Transport Monitor Thread")

	init {
	    isDaemon = true
		name = "Transport Monitor Thread"
	}

	val iterator = NewTransport.executingPool.iterator()

	override fun run() {
		while (NewTransport.enabled) {
			if (isInterrupted) break

			try {
				for (task in iterator) {
					if (task.isFinished()) continue
					if (task.isTimedOut()) {
						logger.warn("Cancelled probably stuck transport task [${task.getExecutionTime()}ms] at ${toVec3i(task.location)}, in ${task.world.name}")
						task.interrupt()
					}
				}
			} catch (e: Throwable) {
				logger.warn("Encountered error when polling transport tasks!")
				e.printStackTrace()
			}

			sleep(1.milliseconds.toJavaDuration())
		}
	}
}
