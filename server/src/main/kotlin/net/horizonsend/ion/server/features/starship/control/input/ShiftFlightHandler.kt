package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import org.bukkit.event.player.PlayerToggleSneakEvent
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class ShiftFlightHandler(controller: PlayerController) : PlayerMovementInputHandler(controller, "Shift Flight") {
	private var sneakMovements = 0

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		sneakMovements = 0
	}

	override fun onBlocked(reason: StarshipMovementException) {
		sneakMovements = 0
	}

	override fun tick() {
		if (starship.type == StarshipType.PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (Hyperspace.isWarmingUp(starship)) {
			starship.controller.userErrorAction("Cannot move while in hyperspace warmup.")
			return
		}

		if (!controller.isSneakFlying()) return

		val now = System.currentTimeMillis()
		if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) return

		starship.lastManualMove = now
		sneakMovements++

		val sneakMovements = sneakMovements

		val maxAccel = starship.balancing.maxSneakFlyAccel
		val accelDistance = starship.balancing.sneakFlyAccelDistance

		val yawRadians = Math.toRadians(controller.yaw.toDouble())
		val pitchRadians = Math.toRadians(controller.pitch.toDouble())

		val distance = max(min(maxAccel, sneakMovements / min(1, accelDistance)), 1)

		val vertical = abs(pitchRadians) >= PI * 5 / 12 // 75 degrees

		val dx = if (vertical) 0 else sin(-yawRadians).roundToInt() * distance
		val dy = sin(-pitchRadians).roundToInt() * distance
		val dz = if (vertical) 0 else cos(yawRadians).roundToInt() * distance

		if (StarshipControl.locationCheck(starship, dx, dy, dz)) return

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}
}
