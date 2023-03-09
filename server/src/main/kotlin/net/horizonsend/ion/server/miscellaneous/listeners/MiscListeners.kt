package net.horizonsend.ion.server.miscellaneous.listeners

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.space.SpaceMechanics
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class MiscListeners : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerLoginEvent(event: AsyncPlayerPreLoginEvent) {
		PlayerData[event.uniqueId]?.update { username = event.name } ?: PlayerData.new(event.uniqueId) { username = event.name }
	}

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
				IonServer.slF4JLogger.warn("\"${player.name}\" has potentially combat logged. \"${otherPlayer.name}\" is ${distance}m away.")
			}
		}
	}

	@EventHandler
	fun onEntityDamage(event: EntityDamageEvent) {
		val player: Player = event.entity as? Player ?: return

		if (event.cause == EntityDamageEvent.DamageCause.DROWNING && SpaceMechanics.isWearingSpaceSuit(player)) {
			event.isCancelled = true
		}
	}
}
