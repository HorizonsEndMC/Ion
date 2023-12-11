package net.horizonsend.ion.server.features.client

import net.horizonsend.ion.server.features.client.networking.Packets
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class VoidNetwork : SLEventListener() {
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
