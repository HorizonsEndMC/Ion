package net.horizonsend.ion.server.features.starship.control

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import kotlin.math.roundToInt

object PlayerStarshipControl : IonServerComponent() {
	fun isHoldingController(player: Player): Boolean {
		val inventory = player.inventory
		return inventory.itemInMainHand.type == StarshipControl.CONTROLLER_TYPE || inventory.itemInOffHand.type == StarshipControl.CONTROLLER_TYPE
	}

	@EventHandler
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		val starship = PilotedStarships[event.player] ?: return
		if (event.itemDrop.itemStack.type != StarshipControl.CONTROLLER_TYPE) return

		event.isCancelled = true
		starship.tryRotate(false)
	}

	@EventHandler
	fun onPlayerSwapItem(event: PlayerSwapHandItemsEvent) {
		val starship = PilotedStarships[event.player] ?: return
		if (event.offHandItem?.type != StarshipControl.CONTROLLER_TYPE) return

		event.isCancelled = true
		starship.tryRotate(true)
	}

	@EventHandler
	fun onPLayerToggleSneak(event: PlayerToggleSneakEvent) {
		PilotedStarships[event.player]?.sneakMovements = 0
	}

	@EventHandler
	fun onPlayerHoldItem(event: PlayerItemHeldEvent) {
		val player = event.player
		val starship = ActiveStarships.findByPilot(player) ?: return

		if (!starship.isDirectControlEnabled) return

		val inventory = player.inventory

		val previousSlot = event.previousSlot
		val oldItem = inventory.getItem(previousSlot)

		val newSlot = event.newSlot
		val newItem = player.inventory.getItem(newSlot)

		inventory.setItem(newSlot, oldItem)
		inventory.setItem(previousSlot, newItem)

		val baseSpeed = StarshipControl.calculateSpeed(newSlot)
		val cooldown: Long = StarshipControl.calculateCooldown(starship.directControlCooldown, newSlot)
		val speed = (10.0f * baseSpeed * (1000.0f / cooldown)).roundToInt() / 10.0f

		player.sendActionBar(text("Speed: $speed", NamedTextColor.AQUA))
	}

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		if (event.player.walkSpeed == 0.009f && PilotedStarships[event.player] == null) {
			event.player.walkSpeed = 0.2f
		}
	}
}
