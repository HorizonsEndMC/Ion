package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
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

		if (!success) return

		starship.controller.onMove(movement)
		starship.subsystems.forEach { runCatching { it.onMovement(movement) } }
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

				starship.sneakMovements = 0
				starship.lastBlockedTime = System.currentTimeMillis()

				completeMovement(starship, movement, false)
			} catch (e: Throwable) {
				starship.serverError("There was an unhandled exception during movement! Please forward this to staff")
				val stackTrace = "$e\n" + e.stackTrace.joinToString(separator = "\n")

				val exceptionMessage =
					ofChildren(
						text(e.message ?: "No message provided", NamedTextColor.RED),
						space(),
						bracketed(text("Hover for info", HEColorScheme.HE_LIGHT_GRAY))
					)
						.hoverEvent(text(stackTrace))
						.clickEvent(ClickEvent.copyToClipboard(stackTrace))

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
