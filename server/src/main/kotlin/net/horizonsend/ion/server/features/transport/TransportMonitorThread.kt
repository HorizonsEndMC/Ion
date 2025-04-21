package net.horizonsend.ion.server.features.transport

import io.netty.util.internal.logging.Slf4JLoggerFactory
import net.horizonsend.ion.server.features.transport.NewTransport.enabled
import net.horizonsend.ion.server.features.transport.NewTransport.executingPool

class TransportMonitorThread : Thread() {
	private val logger = Slf4JLoggerFactory.getInstance(javaClass)

	override fun run() {
		while (enabled) {
			try {
				for (task in executingPool) {
					if (task.isFinished()) continue
					if (task.isTimedOut()) {
						task.interrupt()
					}
				}
			} catch (e: Throwable) {
				logger.warn("Encountered error when polling executing transport tasks!")
				e.printStackTrace()
			}
		}
	}
}
