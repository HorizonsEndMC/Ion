package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object NewWires : IonServerComponent() {
	lateinit var thread: ExecutorService

	override fun onEnable() {
		thread = Executors.newFixedThreadPool(128, Tasks.namedThreadFactory("wire-transport"))
	}

	override fun onDisable() {
		if (::thread.isInitialized) thread.shutdown()
	}


}
