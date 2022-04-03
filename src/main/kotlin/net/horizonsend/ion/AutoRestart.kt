package net.horizonsend.ion

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep
import net.horizonsend.ion.extensions.asMiniMessage
import net.horizonsend.ion.extensions.sendMiniMessage
import org.bukkit.command.CommandSender
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

@CommandAlias("restartASAP")
@CommandPermission("ion.restartASAP")
@Description("Causes the server to restart as soon as it can.")
internal class AutoRestart(private val plugin: Ion): BaseCommand(), Listener {
	private val serverStart = currentTimeMillis()
	private var restartAsSoonAsPossible = false

	// This class is constructed on plugin load, so we can do our init logic here.
	init {
		// Forced 24h restart, the only one we need to give a warning for.
		plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable {
			plugin.server.sendMiniMessage("<aqua>Server restart in 1 minute!")

			sleep(50000) // 50s

			for (i in 10 downTo 2) {
				plugin.server.sendMiniMessage("<red>Server restart in 10 seconds!")
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
		}, 1728000) // 24h
	}

	@EventHandler(priority = MONITOR)
	fun onPlayerDisconnect(event: PlayerQuitEvent) {
		if (!restartAsSoonAsPossible && currentTimeMillis() - serverStart > 21600000) return // 6h

		// The player that just disconnected will still be included in this list
		if (plugin.server.onlinePlayers.size == 1) {
			plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable {
				if (plugin.server.onlinePlayers.isEmpty()) plugin.server.shutdown()
			}, if (restartAsSoonAsPossible) 0 else 6000) // 5m
		}
	}

	@Default
	@Suppress("unused") // Entrypoint (Command)
	fun onRestartASAP(source: CommandSender) {
		restartAsSoonAsPossible = true

		source.server.sendMiniMessage("<aqua>The server has been set to restart as soon as possible in order to apply changes, a restart will occur when all players have disconnected.")
	}
}