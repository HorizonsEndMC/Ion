package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class DirecterControlHandler(controller: PlayerController) : PlayerInputHandler(controller, "Direct-er") {
	override fun handleMove(event: PlayerMoveEvent) {
		val delta = event.to.toVector().subtract(event.from.toVector())

		if (delta.length() > 0.0) {
			event.to = event.from.setDirection(event.to.direction)

			move(Vec3i(delta.multiply(2)))
		}
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		event.isCancelled = true
		move(Vec3i(0, -1, 0))
	}

	override fun handleJump(event: PlayerJumpEvent) {
		event.isCancelled = true
		move(Vec3i(0, 1, 0))
	}

	override fun tick() {}

	fun move(deltaV: Vec3i) {
		val now = System.currentTimeMillis()
		if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) return

		starship.lastManualMove = now
		starship.sneakMovements++

		val (dx, dy, dz) = deltaV

		if (StarshipControl.locationCheck(starship, dx, dy, dz)) return

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}
}
