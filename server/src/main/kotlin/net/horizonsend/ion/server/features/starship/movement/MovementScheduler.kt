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
import java.util.concurrent.ForkJoinPool

object MovementScheduler : IonServerComponent(false) {
	lateinit var thread: ExecutorService
	var handlingMovements: Boolean = false

	override fun onEnable() {
		thread = ForkJoinPool(64)
		thread.execute(::handleMovements)

		handlingMovements = true
	}

	override fun onDisable() {
		if (::thread.isInitialized) {
			thread.shutdownNow()
		}
		handlingMovements = false
	}

	private fun completeMovement(starship: ActiveStarship, movement: StarshipMovement) {
		starship.controller.onMove(movement)
		starship.subsystems.forEach { runCatching { it.onMovement(movement) } }
	}

	@Synchronized
	private fun executeMovement(starship: ActiveControlledStarship, movement: StarshipMovement): Boolean {
		try {
			movement.execute()
			completeMovement(starship, movement)
		} catch (e: StarshipMovementException) {
			val location = if (e is StarshipBlockedException) e.location else null
			starship.controller.onBlocked(movement, e, location)
			starship.controller.sendMessage(e.formatMessage())

			starship.sneakMovements = 0
			starship.lastBlockedTime = System.currentTimeMillis()
			return false
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

			return false
		}

		return true
	}

	private fun handleMovements() {
		while (handlingMovements) runCatching {
			for (starship in ActiveStarships.allControlledStarships()) runCatching shipLoop@{
				val rotationQueue = starship.rotationQueue
				val translateQueue = starship.translationQueue

				// Prioritize rotations
				if (rotationQueue.isNotEmpty()) {
					val movement = rotationQueue.poll() ?: return@shipLoop // Just in case

					movement.future.complete(executeMovement(starship, movement))

					return@shipLoop
				}

				val translation = translateQueue.poll() ?: return@shipLoop
				translation.future.complete(executeMovement(starship, translation))
			}
		}
	}
}
