package net.horizonsend.ion.server.features.starship.active.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object AIManager : IonServerComponent() {
	val activeShips = mutableListOf<ActiveStarship>()

	lateinit var navigationThread: ExecutorService

	override fun onEnable() {
		navigationThread = ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(15), Tasks.namedThreadFactory("ion-ai-pathfinding"))
//		navigationThread = Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("ion-ai-pathfinding"))
	}

	override fun onDisable() {
		if (::navigationThread.isInitialized) navigationThread.shutdown()

		for (activeShip in activeShips) {
			StarshipDestruction.vanish(activeShip)
		}
	}

	fun all() = activeShips

	fun findByController(controller: AIController) = activeShips.firstOrNull { it.controller === controller }
}
