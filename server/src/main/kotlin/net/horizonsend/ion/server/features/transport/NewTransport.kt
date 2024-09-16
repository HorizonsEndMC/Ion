package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.transport.node.manager.TransportManager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object NewTransport : IonServerComponent() {
	private val transportManagers = ConcurrentHashMap.newKeySet<TransportManager>()
	lateinit var thread: ExecutorService

	override fun onEnable() {
		thread = Executors.newFixedThreadPool(128, Tasks.namedThreadFactory("wire-transport"))
	}

	override fun onDisable() {
		if (::thread.isInitialized) thread.shutdown()
	}

	fun registerTransportManager(manager: TransportManager) {
		transportManagers.add(manager)
	}

	fun removeTransportManager(manager: TransportManager) {
		transportManagers.remove(manager)
	}
}
