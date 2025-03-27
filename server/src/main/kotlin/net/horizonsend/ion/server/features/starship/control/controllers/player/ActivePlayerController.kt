package net.horizonsend.ion.server.features.starship.control.controllers.player

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.input.PlayerInput
import net.horizonsend.ion.server.features.starship.control.input.PlayerMovementInputHandler
import net.horizonsend.ion.server.features.starship.control.input.PlayerShiftFlightInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.control.movement.ShiftFlightHandler
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class ActivePlayerController(player: Player, starship: ActiveStarship) : PlayerController(player, starship, "Player") {

	override var movementHandler: MovementHandler = ShiftFlightHandler(this, PlayerShiftFlightInput(this))
		set(value) {
			field.destroy()
			field = value
			value.create()
			information("Updated control mode to ${value.name}")
		}

	override fun tick() {
		if (!starship.isTeleporting) movementHandler.tick()
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		movementHandler.onBlocked(reason)
	}

	companion object : SLEventListener() {
		private fun getMovementHandler(player: Player): MovementHandler? {
			val ship = PilotedStarships[player] ?: return null
			return (ship.controller as? ActivePlayerController)?.movementHandler
		}

		@EventHandler
		fun onPlayerMove(event: PlayerMoveEvent) {
			(getMovementHandler(event.player)?.input as? PlayerInput)?.handleMove(event)
		}

		@EventHandler
		fun onPlayerSneak(event: PlayerToggleSneakEvent) {
			(getMovementHandler(event.player)?.input as? PlayerInput)?.handleSneak(event)
		}

		@EventHandler
		fun onPlayerJump(event: PlayerJumpEvent) {
			(getMovementHandler(event.player)?.input as? PlayerInput)?.handleJump(event)
		}

		@EventHandler
		fun onPlayerHoldItem(event: PlayerItemHeldEvent) {
			(getMovementHandler(event.player)?.input as? PlayerInput)?.handlePlayerHoldItem(event)
		}
	}
}
