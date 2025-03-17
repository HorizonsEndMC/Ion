package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToBlockFace
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import kotlin.math.roundToInt

class DirecterControlHandler(controller: PlayerController) : PlayerMovementInputHandler(controller, "Direct-er") {
	private var accel = 0
	private var lastMove = System.currentTimeMillis()

	private val MAX_ACCEL = 5

	private fun tickAndGetAccel(): Int {
		if (accel >= MAX_ACCEL) return MAX_ACCEL
		accel++
		lastMove = System.currentTimeMillis()
		return accel
	}

	override fun onBlocked(reason: StarshipMovementException) {
		accel = 0
	}

	override fun handleMove(event: PlayerMoveEvent) {
		val accel = tickAndGetAccel()

		val delta = event.to.toVector().subtract(event.from.toVector()).normalize().multiply(accel)

		if (delta.length() > 0.0) {
			val face = vectorToBlockFace(delta)
			if (face == starship.forward) delta.multiply(2)
			event.to = event.from.setDirection(event.to.direction)

			val x = delta.x.roundToInt()
			val y = delta.y.roundToInt()
			val z = delta.z.roundToInt()

			move(Vec3i(x, y, z))
		}
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {}

	override fun handleJump(event: PlayerJumpEvent) {
		val accel = tickAndGetAccel()

		event.isCancelled = true
		move(Vec3i(0, accel, 0))
	}

	override fun tick() {
		val now = System.currentTimeMillis()

		if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) return

		if (now - lastMove > 500 && accel >= 1) {
			accel -= 1
		}

		if (controller.player.isSneaking) move(Vec3i(0, -1, 0))
	}

	fun move(deltaV: Vec3i) {
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
