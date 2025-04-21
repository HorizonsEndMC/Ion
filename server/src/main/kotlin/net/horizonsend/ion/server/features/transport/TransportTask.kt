package net.horizonsend.ion.server.features.transport

import org.slf4j.Logger
import kotlin.properties.Delegates

class TransportTask(
	private val runnable: Runnable,
	private val timeoutMillis: Long,
	private val logger: Logger
) : Runnable {
	private var start by Delegates.notNull<Long>()
	private var finished = false

	fun yield(): Boolean {
		return Thread.interrupted()
	}

	fun isTimedOut(): Boolean {
		val now = System.currentTimeMillis()
		return now < (start + timeoutMillis)
	}

	fun isFinished(): Boolean {
		return finished
	}

	override fun run() {
		start = System.currentTimeMillis()
		NewTransport.executingPool.add(this)

		try {
		    runnable.run()
		}
		catch (e: Throwable) {
			logger.error("Encountered exception when executing async transport task: ${e.message}")
			e.printStackTrace()
		}
		finally {
			NewTransport.executingPool.remove(this)
			finished = true
		}
	}
}
