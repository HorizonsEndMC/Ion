package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.isHoldingController
import org.bukkit.event.player.PlayerToggleSneakEvent

class PlayerShiftFlightInput(override val controller: PlayerController,
) : ShiftFlightInput,PlayerInput {
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
}
