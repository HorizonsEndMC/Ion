package net.horizonsend.ion.server.features.tutorial.tutorials

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.tutorial.message.TutorialMessage
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

abstract class Tutorial {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	/*** Get the number position of this phase**/
	protected fun getOrdinal(tutorialPhase: TutorialPhase): Int = phases.indexOf(tutorialPhase)

	/*** Begin this tutorial for the player**/
	abstract fun startTutorial(player: Player)

	/*** End this tutorial for the player**/
	abstract fun endTutorial(player: Player)

	/**
	 * Run at the initialization of the tutorial. Handle any code needed to set up the tutorial prior to use.
	 **/
	open fun setup() {}

	/**
	 * All the phases of this tutorial. Registered phases are added to this list.
	 **/
	protected val phases = mutableListOf<TutorialPhase>()

	val allPhases get() = phases.toList()

	protected  val playerPhases = mutableMapOf<UUID, TutorialPhase>()
	protected  var readTimes = mutableMapOf<UUID, Long>()

	/** First phase of the tutorial */
	abstract val firstPhase: TutorialPhase
	/** Last phase of the tutorial */
	abstract val lastPhase: TutorialPhase

	/**
	 * Get the current phase of the player in this tutorial
	 **/
	fun getPhase(player: Player): TutorialPhase? = playerPhases[player.uniqueId]
	fun isInTutorial(player: Player): Boolean = playerPhases.containsKey(player.uniqueId)
	fun isReading(player: Player): Boolean = (readTimes[player.uniqueId] ?: 0L) >= System.currentTimeMillis()

	/**
	 * Register this phase. Does not provide access to all available methods of tutorial phases. If those are needed, it should be created and registered manually.
	 **/
	fun registerSimplePhase(
		vararg messages: TutorialMessage,
		cancelEvent: Boolean = true,
		announceCompletion: Boolean = false,
		setupHandlers: TutorialPhase.() -> Unit,
	): TutorialPhase {
		val phase = object : TutorialPhase(this@Tutorial, *messages, cancelEvent = cancelEvent, announceCompletion = announceCompletion) {
			override fun setupHandlers() {
				setupHandlers.invoke(this)
			}

			val skipMessage = ofChildren(
				text("Click "),
				bracketed(text("this"))
					.hoverEvent(text("/tutorial skip ${parent::class.java.simpleName}"))
					.clickEvent(ClickEvent.runCommand("/tutorial skip ${parent::class.java.simpleName}")),
				text(" button to skip to the next phase of the tutorial.")
			)

			override fun onStart(player: Player) {
				player.sendMessage(skipMessage)
			}
		}

		phases.add(phase)

		return phase
	}

	fun registerPhase(phase: TutorialPhase): TutorialPhase {
		phases.add(phase)
		return phase
	}

	abstract class TutorialPhase(
		val parent: Tutorial,
		vararg val messages: TutorialMessage,
		val cancelEvent: Boolean = true,
		val announceCompletion: Boolean = false,
	) {
		open fun setupHandlers() {}

		open fun onStart(player: Player) {}
		open fun onEnd(player: Player) {}

		/**
		 * Runs the code on the given event if the player retrieved from getPlayer
		 * is in the same phase as the phase which called this method in its initialization
		 */
		inline fun <reified T : Event> on(
			crossinline getPlayer: (T) -> Player?,
			crossinline handler: (T, Player) -> Unit,
		) {
			val phase = this

			listen<T>(EventPriority.NORMAL) { event: T ->
				val player: Player = getPlayer(event) ?: return@listen

				if (parent.getPhase(player) == phase) {
					if (parent.isReading(player)) {
						if (event is Cancellable && phase.cancelEvent) {
							event.isCancelled = true
							player.userError("Finish reading the messages! :P")
						}

						return@listen
					}

					handler(event, player)
				}
			}
		}
	}

	fun moveToNextStep(player: Player) {
		val phase: TutorialPhase? = playerPhases[player.uniqueId]
		requireNotNull(phase)

		if (phase.announceCompletion) player.success("Completed $phase")
		player.resetTitle()

		val next: TutorialPhase? = phases.getOrNull(getOrdinal(phase) + 1)

		if (next == null) {
			endTutorial(player) // if there is no next step, then stop instead
			return
		}

		phase.onEnd(player)
		startPhase(player, next)
	}

	fun startPhase(player: Player, phase: TutorialPhase) {
		require(playerPhases.containsKey(player.uniqueId))

		playerPhases[player.uniqueId] = phase

		phase.onStart(player)

		var time = 0L
		for ((index, message) in phase.messages.withIndex()) {
			Tasks.syncDelay(time) {
				if (getPhase(player) == phase) message.show(player)
			}

			if (index == phase.messages.lastIndex) break

			time += (message.seconds * 20).toInt()
		}

		val uuid = player.uniqueId

		Tasks.syncDelay(time + 1) { readTimes.remove(uuid) }

		// add 1 second since this is just in case it doesn't get removed automatically somehow
		readTimes[uuid] = System.currentTimeMillis() + time * 50L + 1000L // 50 ms per tick
	}
}
