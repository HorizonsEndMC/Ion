package net.horizonsend.ion.server.miscellaneous.listeners

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.SpaceMechanics
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class MiscListeners : SLEventListener() {
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
	@Suppress("Unused")
	fun onEntityDamage(event: EntityDamageEvent) {
		val player: Player = event.entity as? Player ?: return

		if (event.cause == EntityDamageEvent.DamageCause.DROWNING && SpaceMechanics.isWearingSpaceSuit(player)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val starship = ActiveStarships.findByPilot(event.player) ?: return

		starship.setDirectControlEnabled(false)
	}
}
