package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class ShiftFlightHandler(controller: PlayerController) : PlayerInputHandler(controller, "Shift Flight") {
	override fun handleMove(event: PlayerMoveEvent) {
		println("move")
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		println("sneak")
	}

	override fun handleJump(event: PlayerJumpEvent) {
		println("jump")
	}

	override fun tick() {
		println("tick")
	}
}
