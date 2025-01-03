package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

object MovementScheduler : IonServerComponent(false) {
	private lateinit var dispatcherThread: ExecutorService
	private lateinit var movementWorker: ExecutorService
	private var handlingMovements: Boolean = false

	override fun onEnable() {
		movementWorker = Executors.newCachedThreadPool(
			object : ThreadFactory {
				private var counter: Int = 0

				override fun newThread(r: Runnable): Thread {
					return Thread(r, "ion-ship-movement-worker-${counter++}")
				}
			}
		)

		dispatcherThread = Executors.newSingleThreadScheduledExecutor()
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
	private fun executeMovement(starship: ActiveControlledStarship, movement: StarshipMovement) = movementWorker.execute {
		synchronized(starship.mutex) {
			try {
				starship.isMoving = true

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
				starship.isMoving = false
			}
		}
	}

	private fun handleMovements() {
		while (handlingMovements) runCatching {
			for (starship in ActiveStarships.allControlledStarships()) runCatching shipLoop@{
				if (starship.isMoving) return@shipLoop

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
