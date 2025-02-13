package net.horizonsend.ion.server.features.starship.movement

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

object MovementScheduler : IonServerComponent(false) {
	private lateinit var dispatcherThread: ExecutorService
	private lateinit var movementWorker: ExecutorService
	private var handlingMovements: Boolean = false

	private val processingLock = ReentrantReadWriteLock()
	private val processingStarships = ObjectOpenHashSet<Starship>()

	fun addProcessingStarship(starship: Starship) {
		processingLock.writeLock().withLock {
			processingStarships.add(starship)
		}
	}

	fun removeProcessingStarship(starship: Starship) {
		processingLock.writeLock().withLock {
			processingStarships.remove(starship)
		}
	}

	fun isProcessingStarship(starship: Starship): Boolean {
		return processingLock.readLock().withLock {
			processingStarships.contains(starship)
		}
	}

	override fun onEnable() {
		movementWorker = Executors.newCachedThreadPool(Tasks.namedThreadFactory("ion-ship-movement-worker"))

		dispatcherThread = Executors.newSingleThreadScheduledExecutor(Tasks.namedThreadFactory("ion-movement-dispatcher"))
		dispatcherThread.execute(::handleMovements)

		handlingMovements = true
	}

	override fun onDisable() {
		handlingMovements = false

		if (::movementWorker.isInitialized) {
			movementWorker.shutdownNow()
		}

		if (::dispatcherThread.isInitialized) {
			movementWorker.shutdownNow()
		}
	}

	private fun completeMovement(starship: ActiveStarship, movement: StarshipMovement, success: Boolean) {
		movement.future.complete(success)

		starship.subsystems.forEach { runCatching { it.onMovement(movement, success) } }

		if (!success) return

		starship.controller.onMove(movement)
	}

	@Synchronized
	private fun executeMovement(starship: ActiveControlledStarship, movement: StarshipMovement) {
		addProcessingStarship(starship)
		starship.isMoving = true

		movementWorker.execute {
			try {
				movement.execute()

				completeMovement(starship, movement, true)
			} catch (e: StarshipMovementException) {
				val location = if (e is StarshipBlockedException) e.location else null
				starship.controller.onBlocked(movement, e, location)
				starship.controller.sendMessage(e.formatMessage())

				starship.lastBlockedTime = System.currentTimeMillis()

				completeMovement(starship, movement, false)
			} catch (e: Throwable) {
				starship.serverError("There was an unhandled exception during movement! Please forward this to staff")
				val exceptionMessage = formatException(e)

				starship.sendMessage(exceptionMessage)

				IonServer.slF4JLogger.error(e.message)
				e.printStackTrace()

				completeMovement(starship, movement, false)
			} finally {
				removeProcessingStarship(starship)
				starship.isMoving = false
			}
		}
	}

	private fun handleMovements() {
		while (handlingMovements) runCatching {
			for (starship in ActiveStarships.allControlledStarships()) runCatching shipLoop@{
				if (starship.isMoving) return@shipLoop
				if (isProcessingStarship(starship)) return@shipLoop

				val rotationQueue = starship.rotationQueue
				val translateQueue = starship.translationQueue

				// Prioritize rotations
				val rotation = rotationQueue.poll()
				if (rotation != null) {
					executeMovement(starship, rotation)

					return@shipLoop
				}

				val translation = translateQueue.poll() ?: return@shipLoop
				executeMovement(starship, translation)
			}
		}
	}
}
