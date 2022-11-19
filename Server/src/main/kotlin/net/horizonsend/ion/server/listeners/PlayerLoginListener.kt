package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerLoginEvent(event: PlayerLoginEvent) {
		PlayerData[event.player.uniqueId].update { minecraftUsername = event.player.name }
	}
}