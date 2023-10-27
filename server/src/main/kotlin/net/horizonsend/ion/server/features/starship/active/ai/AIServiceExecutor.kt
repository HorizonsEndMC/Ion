package net.horizonsend.ion.server.features.starship.active.ai

import com.google.common.collect.HashMultimap
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AIServiceExecutor(val maxPerShip: Int = 10) {
	lateinit var navigationThread: ExecutorService
	val queue = LinkedBlockingQueue<Runnable>()

	fun initialize(): AIServiceExecutor {
		navigationThread = ThreadPoolExecutor(
			1,
			1,
			0L,
			TimeUnit.MILLISECONDS,
			LinkedBlockingQueue(),
			Tasks.namedThreadFactory("ion-ai-pathfinding")
		)

		return this
	}

	fun shutDown() {
		if (::navigationThread.isInitialized) navigationThread.shutdown()
	}

	private val shipMap: HashMultimap<ActiveStarship, CompletableFuture<Void>> = HashMultimap.create()

	fun execute(starship: ActiveStarship, task: Runnable): CompletableFuture<Void> {
		val shipTasks = shipMap[starship]
		if (shipTasks.size >= maxPerShip) throw RejectedExecutionException()

		val futureOutput = CompletableFuture.runAsync(task, navigationThread)
		shipTasks.add(futureOutput)

		futureOutput.thenAccept {
			shipTasks.remove(futureOutput)
		}

		return futureOutput
	}
}
