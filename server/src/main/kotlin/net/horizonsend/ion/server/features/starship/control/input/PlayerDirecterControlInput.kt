package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import kotlin.math.roundToInt

class PlayerDirecterControlInput(override val controller: PlayerController) : DirecterControlInput, PlayerInput {
	override val player get() = controller.player
	override var lastDelta = Vec3i(0,0,0)

	override fun handleMove(event: PlayerMoveEvent) {
		val delta = event.to.toVector().subtract(event.from.toVector()).normalize().multiply(2)

		if (delta.length() > 1e-3) {
			val face = vectorToBlockFace(delta)
			if (face == starship.forward) delta.multiply(2)
			event.to = event.from.setDirection(event.to.direction)

			val x = delta.x.roundToInt()
			val y = delta.y.roundToInt()
			val z = delta.z.roundToInt()

			lastDelta += Vec3i(x, y, z)
		}
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		if (event.isSneaking) lastDelta += Vec3i(0, -1, 0)
	}

	override fun handleJump(event: PlayerJumpEvent) {
		event.isCancelled = true
		lastDelta += Vec3i(0, 1, 0)
	}

	override fun getData(): Vec3i {
		val temp = lastDelta
		lastDelta = Vec3i(0,0,0)
		return temp
	}
}
