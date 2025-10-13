package net.horizonsend.ion.server.features.starship.subsystem.misc.tractor

import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.input.InputHandler
import net.horizonsend.ion.server.features.starship.control.input.PlayerInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl.lastRotationAttempt
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class TractorLookHandler(controller: PlayerController) : MovementHandler(controller, "Tractor Look") {
	override val input: TractorLookInput = TractorLookInput(controller)

	val tractor = controller.starship.tractors.firstOrNull()

	override fun tick() {
		if (tractor == null) return
		val state = tractor.getTowed() ?: return

		popDistanceChanges(tractor, state)
		popRotations(tractor, state)
		trackView(tractor, state)
	}

	fun popDistanceChanges(tug: TractorBeamSubsystem, state: TowedBlocks) {
		val center = state.centerPoint ?: return
		var direction: RelativeFace? = null

		while (input.distanceQueue.isNotEmpty()) {
			val vector = input.distanceQueue.removeFirst()
			direction = vector
		}

		if (direction == null) return
		//TODO improve that

		val eyeLocation = starship.playerPilot?.eyeLocation ?: return

		val vector = center.toCenterVector().clone().subtract(eyeLocation.toVector()).normalize()
		if (direction == RelativeFace.BACKWARD) vector.multiply(-1)

		val dx = vector.x.roundToInt()
		val dy = vector.y.roundToInt()
		val dz = vector.z.roundToInt()

		state.move(tug.starship.world, TransformationAccessor.TranslationTransformation(null, dx, dy, dz))
	}

	fun popRotations(tug: TractorBeamSubsystem, state: TowedBlocks) {
		val middle = state.centerPoint ?: return

		var fullRotation = 0.0

		while (input.queuedRotations.isNotEmpty()) {
			val theta = input.queuedRotations.removeFirst()
			fullRotation += theta
		}

		if ((fullRotation % 360.0) == 0.0) return

		state.move(tug.starship.world, TransformationAccessor.RotationTransformation(null, fullRotation) { middle })
	}

	var lastDirection: Vector? = starship.playerPilot?.location?.direction

	fun trackView(tug: TractorBeamSubsystem, state: TowedBlocks) {
		val center = state.centerPoint ?: return
		val playerPilot = starship.playerPilot?: return
		val viewDirection = playerPilot.location.direction

		if (viewDirection == lastDirection) return
		lastDirection = viewDirection

		val vector = center.toCenterVector().clone().subtract(playerPilot.eyeLocation.toVector())
		val distance = vector.length()

		val newLocation = viewDirection.clone().normalize().multiply(distance).add(playerPilot.eyeLocation.toVector())

		val difference = newLocation.clone().subtract(center.toCenterVector())

		val dx = difference.x.roundToInt()
		val dy = difference.y.roundToInt()
		val dz = difference.z.roundToInt()

		state.move(tug.starship.world, TransformationAccessor.TranslationTransformation(null, dx, dy, dz))
	}

	inner class TractorLookInput(override val controller: PlayerController) : InputHandler, PlayerInput {
		override fun getData(): Any = Any()
		override val player: Player = controller.player

		val distanceQueue = ArrayDeque<RelativeFace>()
		val queuedRotations = ArrayDeque<Double>()

		override fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {
			if (tractor == null) return
			event.isCancelled = true

			if (event.player.inventory.getItem(event.previousSlot)?.type != StarshipControl.CONTROLLER_TYPE) return

			val old = event.previousSlot
			val new = event.newSlot

			if (new == 0 && old == 8) {
				distanceQueue.addLast(RelativeFace.FORWARD)
				return
			}

			if (new == 8 && old == 0) {
				distanceQueue.addLast(RelativeFace.BACKWARD)
				return
			}

			if (old - new < 0) {
				distanceQueue.addLast(RelativeFace.BACKWARD)
			}
			else distanceQueue.addLast(RelativeFace.FORWARD)
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
