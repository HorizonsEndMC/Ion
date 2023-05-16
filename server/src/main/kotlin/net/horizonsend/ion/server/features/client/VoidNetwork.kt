package net.horizonsend.ion.server.features.client

import net.horizonsend.ion.server.features.client.networking.Packets
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class VoidNetwork : Listener {
	@EventHandler
	fun handshakeUser(event: PlayerJoinEvent) = Packets.HANDSHAKE.send(event.player)

	@EventHandler
	fun quitModUser(event: PlayerQuitEvent) {
		if (modUsers.contains(event.player.uniqueId)) {
			modUsers.remove(event.player.uniqueId)
			Packets.PLAYER_REMOVE.broadcast(event.player)
		}
	}

	companion object {
		val modUsers = mutableListOf<UUID>()
	}
}
