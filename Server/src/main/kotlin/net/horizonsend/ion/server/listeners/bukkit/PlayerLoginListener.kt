package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.common.utilities.constructPlayerListName
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener: Listener {
	@EventHandler
	fun onPlayerLoginEvent(event: PlayerLoginEvent) {
		constructPlayerListName(event.player.name, event.player.uniqueId).thenAcceptAsync {
			event.player.playerListName(it)
		}
	}
}