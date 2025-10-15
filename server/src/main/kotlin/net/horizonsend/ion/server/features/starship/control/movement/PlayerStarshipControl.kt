package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID

object PlayerStarshipControl : IonServerComponent() {
	fun isHoldingController(player: Player): Boolean {
		val inventory = player.inventory
		return inventory.itemInMainHand.type == StarshipControl.CONTROLLER_TYPE || inventory.itemInOffHand.type == StarshipControl.CONTROLLER_TYPE
	}

	val lastRotationAttempt = mutableMapOf<UUID, Long>()

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		if (event.player.walkSpeed == 0.009f && PilotedStarships[event.player] == null) {
			event.player.walkSpeed = 0.2f
		}
	}
}
