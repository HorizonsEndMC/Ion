package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.input.DirecterControlInput
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class DirecterControlHandler(
	controller: PlayerController,
	override val input: DirecterControlInput
) : MovementHandler(controller, "Direct-er", input) {
	override fun tick() {
		val deltaV = input.getData()
		val now = System.currentTimeMillis()
		if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) return

		starship.lastManualMove = now

		var (dx, dy, dz) = deltaV

		if (starship.type == StarshipType.TANK) {
			dy = ShiftFlightHandler.getHoverHeight(starship, Vec3i(dx, 0, dy))
		}

		if (StarshipControl.locationCheck(starship, dx, dy, dz)) return

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}
}
