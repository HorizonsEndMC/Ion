package net.horizonsend.ion.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandSendEvent

/** Used to hide commands from tab completion. Work around for https://github.com/aikar/commands/issues/380. */
class PlayerCommandSendListener : Listener {
	@EventHandler
	fun onPlayerCommandSendEvent(event: PlayerCommandSendEvent) {
		event.commands.removeIf {
			when (it) {
				"dc", "directcontrol" -> true
				else -> false
			}
		}
	}
}
