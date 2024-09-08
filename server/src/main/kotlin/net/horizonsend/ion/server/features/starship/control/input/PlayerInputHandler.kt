package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

abstract class PlayerInputHandler(val controller: PlayerController, val name: String) {
	val starship = controller.starship

	abstract fun handleMove(event: PlayerMoveEvent)
	abstract fun handleSneak(event: PlayerToggleSneakEvent)
	abstract fun handleJump(event: PlayerJumpEvent)

	abstract fun tick()
}
