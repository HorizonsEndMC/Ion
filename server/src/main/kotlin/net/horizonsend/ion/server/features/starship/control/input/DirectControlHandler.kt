package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class DirectControlHandler(controller: PlayerController) : PlayerInputHandler(controller, "Direct Control") {
	override fun handleMove(event: PlayerMoveEvent) {
		TODO("Not yet implemented")
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		TODO("Not yet implemented")
	}

	override fun handleJump(event: PlayerJumpEvent) {
		TODO("Not yet implemented")
	}

	override fun tick() {
		TODO("Not yet implemented")
	}
}
