package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.lastRotationAttempt
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class PlayerShiftFlightInput(override val controller: PlayerController) : ShiftFlightInput, PlayerInput {
	override val player get() = controller.player
	override var pitch : Float
		get() = player.pitch
		set(value) {}
	override var yaw : Float
		get() = player.yaw
		set(value) {}
	override var isSneakFlying : Boolean
		get() = player.isSneaking && isHoldingController(player)
		set(value) {}
	override var toggledSneak = false

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		toggledSneak = true
	}
	override fun getData(): ShiftFlightInput.ShiftFlightData {
		val temp = toggledSneak
		toggledSneak = false
		return ShiftFlightInput.ShiftFlightData(pitch, yaw, isSneakFlying, temp)
	}

	override fun handleDropItem(event: PlayerDropItemEvent) {
		if (event.itemDrop.itemStack.type != StarshipControl.CONTROLLER_TYPE) return

		event.isCancelled = true

		if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

		lastRotationAttempt[event.player.uniqueId] = System.currentTimeMillis()
		starship.tryRotate(false)
	}

	override fun handleSwapHands(event: PlayerSwapHandItemsEvent) {
		if (event.offHandItem.type != StarshipControl.CONTROLLER_TYPE) return

		event.isCancelled = true

		if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

		starship.tryRotate(true)
	}
}
