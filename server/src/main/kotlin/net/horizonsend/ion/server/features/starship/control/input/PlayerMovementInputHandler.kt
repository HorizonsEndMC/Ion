package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

abstract class PlayerMovementInputHandler(val controller: PlayerController, val name: String) {
	val starship = controller.starship

	open fun create() {}
	open fun destroy(new: PlayerMovementInputHandler) {}

	open fun handleMove(event: PlayerMoveEvent) {}
	open fun handleSneak(event: PlayerToggleSneakEvent) {}
	open fun handleJump(event: PlayerJumpEvent) {}
	open fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {}

	open fun onBlocked(reason: StarshipMovementException) {}

	abstract fun tick()
}
