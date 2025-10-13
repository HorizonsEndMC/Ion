package net.horizonsend.ion.server.features.starship.subsystem.misc.tractor

import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.input.InputHandler
import net.horizonsend.ion.server.features.starship.control.input.PlayerInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.lastRotationAttempt
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class TractorWASDHandler(controller: PlayerController) : MovementHandler(controller, "Tractor Direct") {
	override val input: TractorWASDInput = TractorWASDInput(controller)

	val tractor = controller.starship.tractors.firstOrNull()

	override fun tick() {
		if (tractor == null) return
		val state = tractor.getTowed() ?: return

		popTranslations(state)
		popRotations(state)
	}

	fun popTranslations(state: TowedBlocks) {
		val delta = Vector()

		while (input.moveVectorDeque.isNotEmpty()) {
			val vector = input.moveVectorDeque.removeFirst()
			delta.add(vector)
		}

		if (delta.isZero) return

		val dx = delta.x.roundToInt()
		val dy = delta.y.roundToInt()
		val dz = delta.z.roundToInt()

		state.move(TransformationAccessor.TranslationTransformation(null, dx, dy, dz))
	}

	fun popRotations(state: TowedBlocks) {
		val middle = state.centerPoint ?: return

		var fullRotation = 0.0

		while (input.queuedRotations.isNotEmpty()) {
			val theta = input.queuedRotations.removeFirst()
			fullRotation += theta
		}

		if ((fullRotation % 360.0) == 0.0) return

		state.move(TransformationAccessor.RotationTransformation(null, fullRotation) { middle })
	}

	inner class TractorWASDInput(override val controller: PlayerController) : InputHandler, PlayerInput {
		override fun getData(): Any = Any()
		override val player: Player = controller.player

		val moveVectorDeque = ArrayDeque<Vector>()
		val queuedRotations = ArrayDeque<Double>()

		override fun handleMove(event: PlayerMoveEvent) {
			if (tractor == null) return

			if (!event.hasChangedPosition()) return

			if (player.isSneaking) return

			event.isCancelled = true
			if (!tractor.towState.canStartDiscovery()) return

			val difference = event.to.clone().subtract(event.from).toVector().normalize()

			moveVectorDeque.addLast(difference)
		}

		override fun handleSneak(event: PlayerToggleSneakEvent) {
			if (tractor == null) return

			event.isCancelled = true
			if (!tractor.towState.canStartDiscovery()) return

			moveVectorDeque.addLast(Vector(0.0, -1.0, 0.0))
		}

		override fun handleSwapHands(event: PlayerSwapHandItemsEvent) {
			if (event.offHandItem.type != StarshipControl.CONTROLLER_TYPE) return

			event.isCancelled = true

			if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

			queuedRotations.add(90.0)
		}

		override fun handleDropItem(event: PlayerDropItemEvent) {
			if (event.itemDrop.itemStack.type != StarshipControl.CONTROLLER_TYPE) return

			event.isCancelled = true

			if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

			lastRotationAttempt[event.player.uniqueId] = System.currentTimeMillis()

			queuedRotations.add(270.0)
		}
	}
}
