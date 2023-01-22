package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerQuitEvent(event: PlayerQuitEvent) {
		val player = event.player

		// Remove Disconnect Message
		event.quitMessage(null)

		// Log if player has potentially combat logged
		for (otherPlayer in player.world.players) {
			if (player == otherPlayer) continue // Same Player
			if (otherPlayer.hasMetadata("NPC")) continue // Citizens NPC

			val distance = player.location.distance(otherPlayer.location)

			if (distance <= 1_000) {
				Ion.slF4JLogger.warn("\"${player.name}\" has potentially combat logged. \"${otherPlayer.name}\" is ${distance}m away.")
			}
		}
	}
}
