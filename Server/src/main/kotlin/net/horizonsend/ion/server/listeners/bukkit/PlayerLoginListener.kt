package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.utilities.constructPlayerListNameAsync
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerLoginListener: Listener {
	@EventHandler
	fun onPlayerLoginEvent(event: PlayerLoginEvent) {
		constructPlayerListNameAsync(event.player.name, event.player.uniqueId).thenAcceptAsync {
			event.player.playerListName(it)
		}

		// Ensure the player exists in the database
		transaction { PlayerData.getOrCreate(event.player.uniqueId, event.player.name) }
	}
}