package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.transport.node.manager.TransportManager
import net.horizonsend.ion.server.features.transport.old.Extractors
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.fixedRateTimer

object NewTransport : IonServerComponent(runAfterTick = true /* Run after tick to wait on the full server startup. */) {
	private val transportManagers = ConcurrentHashMap.newKeySet<TransportManager>()

	lateinit var monitorThread: Timer
	lateinit var executor: ExecutorService

	override fun onEnable() {
		executor = Executors.newFixedThreadPool(64, Tasks.namedThreadFactory("wire-transport"))

		val interval: Long = (1000 / Extractors.extractorTicksPerSecond).toLong()
		monitorThread = fixedRateTimer(name = "Extractor Tick", daemon = true, initialDelay = interval, period = interval) {
			transportManagers.forEach {
				try {
					it.tick()
				} catch (exception: Exception) {
					exception.printStackTrace()
				}
			}
		}
	}

	override fun onDisable() {
		if (::monitorThread.isInitialized) monitorThread.cancel()
		if (::executor.isInitialized) executor.shutdown()
	}

	fun registerTransportManager(manager: TransportManager) {
		transportManagers.add(manager)
	}

	fun removeTransportManager(manager: TransportManager) {
		transportManagers.remove(manager)
	}
}
