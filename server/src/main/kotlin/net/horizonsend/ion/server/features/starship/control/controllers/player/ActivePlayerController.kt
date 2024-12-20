package net.horizonsend.ion.server.features.starship.control.controllers.player

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.input.PlayerMovementInputHandler
import net.horizonsend.ion.server.features.starship.control.input.ShiftFlightHandler
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class ActivePlayerController(player: Player, starship: ActiveStarship) : PlayerController(player, starship, "Player") {
	override fun isSneakFlying(): Boolean = player.isSneaking && PlayerStarshipControl.isHoldingController(player)
	override val selectedDirectControlSpeed: Int get() = player.inventory.heldItemSlot

	var inputHandler: PlayerMovementInputHandler = ShiftFlightHandler(this)
		set(value) {
			field.destroy(value)
			field = value
			value.create()
			information("Updated control mode to ${value.name}")
		}

	override fun tick() {
		if (!starship.isTeleporting) inputHandler.tick()
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		inputHandler.onBlocked(reason)
	}

	companion object : SLEventListener() {
		private fun getInputHandler(player: Player): PlayerMovementInputHandler? {
			val ship = PilotedStarships[player] ?: return null
			return (ship.controller as? ActivePlayerController)?.inputHandler
		}

		@EventHandler
		fun onPlayerMove(event: PlayerMoveEvent) {
			getInputHandler(event.player)?.handleMove(event)
		}

		@EventHandler
		fun onPlayerSneak(event: PlayerToggleSneakEvent) {
			getInputHandler(event.player)?.handleSneak(event)
		}

		@EventHandler
		fun onPlayerJump(event: PlayerJumpEvent) {
			getInputHandler(event.player)?.handleJump(event)
		}


		@EventHandler
		fun onPlayerHoldItem(event: PlayerItemHeldEvent) {
			getInputHandler(event.player)?.handlePlayerHoldItem(event)
		}
	}
}
