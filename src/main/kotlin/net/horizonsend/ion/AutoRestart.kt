package net.horizonsend.ion

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import java.lang.Math.floorDiv
import java.lang.System.currentTimeMillis
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

/**
 * This class determines whether the server should be restarted dependent on a number of rules, in order of priority:
 * 1) Do not restart if uptime is less than 6 hours.
 * 2) Restart if uptime is 24 hours.
 * 3) Only restart if there has been no online players within the past 5 minutes.
 *
 * If restartAsSoonAsPossible is set to true, then rule 1 is ignored, and rule 3 will not wait 5 minutes.
 */

@CommandAlias("restartvote")
@Description("Causes the server to restart as soon as it can.")
internal class AutoRestart(private val plugin: Ion): BaseCommand(), Listener {
	private val requiredRestartVotes get() = floorDiv(plugin.server.onlinePlayers.size, 2) + 1

	private var isRestartVoting = false
	private var restartVotes = mutableSetOf<Player>()

	private val serverStart = currentTimeMillis()

	// This class is constructed on plugin load, so we can do our init logic here.
	init {
		// Forced 24h restart, the only one we need to give a warning for.
		plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable { restartCountdown() }, 1728000) // 24h
	}

	fun restartCountdown() {
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
	}

	@EventHandler(priority = MONITOR)
	fun onPlayerDisconnect(@Suppress("unused") event: PlayerQuitEvent) { // Event
		if (currentTimeMillis() - serverStart < 21600000) return // 6h

		// The player that just disconnected will still be included in this list
		if (plugin.server.onlinePlayers.size == 1) {
			plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable {
				if (plugin.server.onlinePlayers.isEmpty()) plugin.server.shutdown()
			}, 6000) // 5m
		}
	}

	@Default
	@CommandPermission("ion.restartvote")
	@Suppress("unused") // Entrypoint (Command)
	fun onRestartVote(sender: CommandSender) {
		if (plugin.server.onlinePlayers.isEmpty()) plugin.server.shutdown()

		plugin.server.sendMiniMessage("<aqua><b>The server needs to restart to apply changes!</b></aqua> Click <blue><underlined><click:run_command:'/restartvoteyes'>here</click></underlined></blue> to vote yes to the restart. Restart will take place after $requiredRestartVotes votes within the next 2 minutes.")
		isRestartVoting = true

		plugin.server.scheduler.runTaskLater(plugin, Runnable {
			restartVotes.clear()
			isRestartVoting = false
		}, 20*60*2)
	}

	@CommandAlias("restartvoteyes")
	@Suppress("unused") // Entrypoint (Command)
	fun onRestartVoteYet(sender: Player) {
		if (!isRestartVoting) {
			sender.sendMiniMessage("<yellow>There is not currently a server restart vote.")
			return
		}

		if (restartVotes.size >= requiredRestartVotes) {
			plugin.server.sendMiniMessage("<aqua>Required votes met, server will restart.")
			restartCountdown()
			return
		}

		restartVotes.add(sender)

		plugin.server.sendMiniMessage("<aqua>There are now ${restartVotes.size} restart votes. $requiredRestartVotes are needed.")
	}
}
