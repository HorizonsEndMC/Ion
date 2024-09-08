package net.horizonsend.ion.server.features.starship.control.controllers.player

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.input.PlayerInputHandler
import net.horizonsend.ion.server.features.starship.control.input.ShiftFlightHandler
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class ActivePlayerController(player: Player, starship: ActiveStarship) : PlayerController(player, starship, "Player") {
	override fun isSneakFlying(): Boolean = player.isSneaking && PlayerStarshipControl.isHoldingController(player)
	override val selectedDirectControlSpeed: Int get() = player.inventory.heldItemSlot

	var inputHandler: PlayerInputHandler = ShiftFlightHandler(this)
		set(value) {
			field = value
			information("Updated control move to ${value.name}")
		}

	override fun tick() {
		inputHandler.tick()
	}

	companion object : SLEventListener() {
		private fun getInputHandler(player: Player): PlayerInputHandler? {
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
	}
}
