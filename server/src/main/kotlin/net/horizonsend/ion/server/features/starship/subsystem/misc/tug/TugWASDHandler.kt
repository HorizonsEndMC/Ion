package net.horizonsend.ion.server.features.starship.subsystem.misc.tug

import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.input.InputHandler
import net.horizonsend.ion.server.features.starship.control.input.PlayerInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class TugWASDHandler(controller: PlayerController) : MovementHandler(controller, "Tug Direct") {
	override val input: TugWASDInput = TugWASDInput(controller)

	val tug = controller.starship.tugs.firstOrNull()


	override fun tick() {
		if (tug == null) return
		if (tug.movedBlocks.isEmpty()) return

		val delta = Vector()

		while (input.moveVectorDequeue.isNotEmpty()) {
			val vector = input.moveVectorDequeue.removeFirst()
			delta.add(vector)
		}

		if (delta.isZero) return

		val dx = delta.x.roundToInt()
		val dy = delta.y.roundToInt()
		val dz = delta.z.roundToInt()

		tug.handleMovement(TransformationAccessor.TranslationTransformation(null, dx, dy, dz))
	}

	inner class TugWASDInput(override val controller: PlayerController) : InputHandler, PlayerInput {
		override fun getData(): Any = Any()
		override val player: Player = controller.player

		val moveVectorDequeue = ArrayDeque<Vector>()

		override fun handleMove(event: PlayerMoveEvent) {
			if (tug == null) return

			if (!event.hasChangedPosition()) return

			if (player.isSneaking) return

			event.isCancelled = true
			if (tug.lastTaskFuture?.isDone == false) return

			val difference = event.to.clone().subtract(event.from).toVector().normalize()

			moveVectorDequeue.addLast(difference)
		}

		override fun handleSneak(event: PlayerToggleSneakEvent) {
			if (tug == null) return

			event.isCancelled = true
			if (tug.lastTaskFuture?.isDone == false) return

			moveVectorDequeue.addLast(Vector(0.0, -1.0, 0.0))
		}
	}
}
