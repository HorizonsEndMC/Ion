package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener : Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerLoginEvent(event: PlayerLoginEvent) {
		PlayerData[event.player.uniqueId].update { minecraftUsername = event.player.name }
	}
}