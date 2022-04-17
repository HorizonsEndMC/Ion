package net.horizonsend.ion

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Private
import java.lang.Math.floorDiv
import java.lang.Thread.sleep
import net.horizonsend.ion.extensions.asMiniMessage
import net.horizonsend.ion.extensions.sendMiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent.Cause.RESTART_COMMAND
import org.bukkit.event.player.PlayerQuitEvent

internal class Restart(private val plugin: Ion): BaseCommand(), Listener {
	private val isVoteRestartInProgress = false

	private val requiredRestartVotes get() = floorDiv(plugin.server.onlinePlayers.size, 2) + 1

	private var isRestartVoting = false
	private var restartVotes = mutableSetOf<Player>()

	private fun doRestartCountdown() {
		plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
			plugin.server.sendMiniMessage("<aqua>Server restart in 1 minute!")

			sleep(50000) // 50s

			for (i in 10 downTo 2) {
				plugin.server.sendMiniMessage("<red>Server restart in $i seconds!")
				sleep(1000) // 1s
			}

			plugin.server.sendMiniMessage("<red>Server restart in 1 second!")
			sleep(1000) // 1s

			plugin.server.scheduler.runTask(plugin, Runnable {
				plugin.server.onlinePlayers.forEach {
					it.kick("<aqua>Server is restarting.".asMiniMessage, RESTART_COMMAND)
				}
				plugin.server.shutdown()
			})
		})
	}

	@EventHandler(priority = MONITOR)
	fun onPlayerDisconnect(@Suppress("unused") event: PlayerQuitEvent) { // Event
		restartVotes.remove(event.player)
	}

	@CommandAlias("restartvote")
	@CommandPermission("ion.restartvote")
	@Description("Starts a vote to restart the server.")
	@Suppress("unused") // Entrypoint (Command)
	fun onRestartVote(sender: CommandSender) {
		if (isVoteRestartInProgress) {
			sender.sendMiniMessage("<yellow>There is already a vote to restart the server in progress.")
			return
		}

		if (plugin.server.onlinePlayers.isEmpty()) {
			plugin.server.shutdown()
			return
		}

		plugin.server.sendMiniMessage("<aqua><b>The server needs to restart to apply changes!</b></aqua> Click <blue><underlined><click:run_command:'/restartvoteyes'>here</click></underlined></blue> to vote yes to the restart. Restart will take place after $requiredRestartVotes votes within the next 2 minutes.")

		isRestartVoting = true

		plugin.server.scheduler.runTaskLater(plugin, Runnable {
			plugin.server.sendMiniMessage("<red>Failed to get enough restart votes.")
			restartVotes.clear()
			isRestartVoting = false
		}, 20*60*2)
	}

	@CommandAlias("restartvoteyes")
	@Private
	@Suppress("unused") // Entrypoint (Command)
	fun onRestartVoteYes(sender: Player) {
		if (!isRestartVoting) {
			sender.sendMiniMessage("<yellow>There is not currently a server restart vote.")
			return
		}

		if (restartVotes.contains(sender)) {
			sender.sendMiniMessage("<yellow>You have already voted.")
			return
		}

		if (restartVotes.size > requiredRestartVotes) {
			plugin.server.sendMiniMessage("<aqua>Required votes met, server will restart.")
			doRestartCountdown()
			return
		}

		restartVotes.add(sender)

		plugin.server.sendMiniMessage("<aqua>There are now ${restartVotes.size} restart votes. $requiredRestartVotes are needed.")
	}
}
