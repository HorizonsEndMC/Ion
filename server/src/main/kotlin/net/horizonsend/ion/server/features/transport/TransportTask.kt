package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.World
import org.slf4j.Logger

class TransportTask(
	val location: BlockKey,
	val world: World,
	private val runnable: TransportTask.() -> Unit,
	private val timeoutMillis: Long,
	private val logger: Logger
) : Runnable {
	private var interrupted: Boolean = false
	private var start = -1L
	private var finished = false

	fun interrupt() {
		interrupted = true
	}

	fun isInterrupted(): Boolean = interrupted

	fun getExecutionTime(): Long {
		return System.currentTimeMillis() - start
	}

	fun isTimedOut(): Boolean {
		if (start == -1L || finished) return false
		return getExecutionTime() > timeoutMillis
	}

	fun isFinished(): Boolean {
		return finished
	}

	override fun run() {
		start = System.currentTimeMillis()
		NewTransport.executingPool.add(this)

		try {
		    runnable.invoke(this)
		}
		catch (e: Throwable) {
			logger.error("Encountered exception when executing async transport task at ${toVec3i(location)}, in ${world.name}: ${e.message}")
			e.printStackTrace()
		}
		finally {
			NewTransport.executingPool.remove(this)
			finished = true
		}
	}
}
