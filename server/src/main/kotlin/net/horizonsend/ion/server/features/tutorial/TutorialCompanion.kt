package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

abstract class TutorialCompanion {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	var playersInTutorials = mutableMapOf<Player, TutorialPhase>()
	private var readTimes = mutableMapOf<UUID, Long>()

	open fun onEnable() {}
	open fun onDisable() {}


	abstract val WORLD_NAME: String

	fun getWorld(): World = Bukkit.getWorld(WORLD_NAME)!!
	fun isTutorialWorld(world: World) = getWorld().uid == world.uid

	abstract val entries: List<TutorialPhase>
	val FIRST: TutorialPhase get() = entries.first()
	val LAST: TutorialPhase get() = entries.last()

	abstract fun teleportToStart(player: Player)
	abstract fun teleportToEnd(player: Player)

	protected abstract fun startTutorial(player: Player)

	fun start(player: Player) {
		startTutorial(player)

		startPhase(player, FIRST)
	}

	fun startPhase(player: Player, phase: TutorialPhase) {
		require(Tutorials.playersInTutorials.containsKey(player))

		Tutorials.playersInTutorials[player] = phase

		phase.onStart(player)

		var time = 0L
		for ((index, message) in phase.messages.withIndex()) {
			Tasks.syncDelay(time) {
				if (getPhase(player) == phase) message.show(player)
			}

			if (index == phase.messages.lastIndex) {
				break
			}

			time += (message.seconds * 20).toInt()
		}

		val uuid = player.uniqueId

		Tasks.syncDelay(time + 1) { readTimes.remove(uuid) }

		// add 1 second since this is just in case it doesn't get removed automatically somehow
		readTimes[uuid] = System.currentTimeMillis() + time * 50L + 1000L // 50 ms per tick
	}

	fun stop(player: Player) {
		readTimes.remove(player.uniqueId)

		val phase: TutorialPhase? = playersInTutorials.remove(player)

		if (!Tutorials.isTutorialWorld(player.world)) return

		player.resetTitle()

		Tasks.syncDelay(10) {
			when (phase) {
				FlightTutorialPhase.LAST -> teleportToEnd(player)
				else -> teleportToStart(player)
			}
		}
	}

	fun isReading(player: Player): Boolean = (readTimes[player.uniqueId] ?: 0L) >= System.currentTimeMillis()

	fun getPhase(player: Player): TutorialPhase? = playersInTutorials[player]

	/**
	 * Runs the code on the given event if the player retrieved from getPlayer
	 * is in the same phase as the phase which called this method in its initialization
	 */
	inline fun <reified T : Event> TutorialPhase.on(
		crossinline getPlayer: (T) -> Player?,
		crossinline handler: (T, Player) -> Unit,
	) {
		val phase = this

		listen<T>(EventPriority.NORMAL) { event: T ->
			val player: Player = getPlayer(event) ?: return@listen

			if (getPhase(player) == phase) {
				if (isReading(player)) {
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

	fun TutorialPhase.moveToNextStep(player: Player) {
		if (announceCompletion) player.success("Completed $this")
		player.resetTitle()

		val next: TutorialPhase? = entries.getOrNull(ordinal + 1)

		if (next == null) {
			stop(player) // if there is no next step, then stop instead
			return
		}

		onEnd(player)
		startPhase(player, next)
	}
}
