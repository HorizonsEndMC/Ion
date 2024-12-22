package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import java.util.UUID
import kotlin.math.roundToInt

object PlayerStarshipControl : IonServerComponent() {
	fun isHoldingController(player: Player): Boolean {
		val inventory = player.inventory
		return inventory.itemInMainHand.type == StarshipControl.CONTROLLER_TYPE || inventory.itemInOffHand.type == StarshipControl.CONTROLLER_TYPE
	}

	val lastRotationAttempt = mutableMapOf<UUID, Long>()

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val starship = PilotedStarships[event.player] ?: return
		if (event.itemDrop.itemStack.type != StarshipControl.CONTROLLER_TYPE) return

		event.isCancelled = true

		if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

		lastRotationAttempt[event.player.uniqueId] = System.currentTimeMillis()
		starship.tryRotate(false)
	}

	@EventHandler
	fun onPlayerSwapItem(event: PlayerSwapHandItemsEvent) {
		val starship = PilotedStarships[event.player] ?: return
		if (event.offHandItem.type != StarshipControl.CONTROLLER_TYPE) return

		event.isCancelled = true

		if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

		starship.tryRotate(true)
	}

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		if (event.player.walkSpeed == 0.009f && PilotedStarships[event.player] == null) {
			event.player.walkSpeed = 0.2f
		}
	}
}
