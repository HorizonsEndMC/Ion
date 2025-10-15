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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class TractorLookHandler(controller: PlayerController) : MovementHandler(controller, "Tractor Look") {
	override val input: TractorLookInput = TractorLookInput(controller)

	val tractor = controller.starship.tractors.firstOrNull()

	private var sneakMovements = 0

	override fun tick() {
		if (tractor == null) return
		val state = tractor.getTowed() ?: return

		popDistanceChanges(tractor, state)
		popRotations(tractor, state)
		trackView(tractor, state)
	}

	var lastDistanceChangeMillis = System.currentTimeMillis()

	fun popDistanceChanges(tug: TractorBeamSubsystem, state: TowedBlocks) {
		val center = state.centerPoint ?: return
		var direction: RelativeFace? = null

		val now = System.currentTimeMillis()
		if (now - lastDistanceChangeMillis < state.manualMoveCooldown) {
			return
		}
		lastDistanceChangeMillis = now

		if (input.distanceQueue.isEmpty()) {
			sneakMovements = 0
		}

		// Only check after cooldown
		while (input.distanceQueue.isNotEmpty()) {
			val vector = input.distanceQueue.removeFirst()
			direction = vector
		}

		if (direction == null) return
		//TODO improve that

		val eyeLocation = starship.playerPilot?.eyeLocation ?: return

		sneakMovements++

		val sneakMovements = sneakMovements

		val accelDistance = (6.0 - min(state.blocks.size.toDouble(), 1_000_000.0).pow(1.0 / 8.0)).roundToInt()
		val distance = max(min(4, sneakMovements / min(1, accelDistance)), 1)

		val vector = center.toCenterVector().clone().subtract(eyeLocation.toVector()).normalize()
		if (direction == RelativeFace.BACKWARD) vector.multiply(-1)

		val dx = vector.x.roundToInt() * distance
		val dy = vector.y.roundToInt() * distance
		val dz = vector.z.roundToInt() * distance

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
	private var lastTrackMovementTimeMillis = System.currentTimeMillis()

	private var trackMovements = 0

	fun trackView(tug: TractorBeamSubsystem, state: TowedBlocks) {
		val center = state.centerPoint ?: return
		val playerPilot = starship.playerPilot?: return
		val viewDirection = playerPilot.location.direction

		val now = System.currentTimeMillis()
		if (now - lastTrackMovementTimeMillis < state.manualMoveCooldown) {
			return
		}

		if (viewDirection == lastDirection) {
			trackMovements = 0
		}

		lastTrackMovementTimeMillis = now

		val vectorToTowCenter = center.toCenterVector().clone().subtract(playerPilot.eyeLocation.toVector())
		val distanceToTowCenter = vectorToTowCenter.length()

		if (vectorToTowCenter.angle(viewDirection) < Math.toRadians(5.0)) return

		trackMovements++

		val trackMovements = trackMovements

		val accelDistance = (6.0 - min(state.blocks.size.toDouble(), 1_000_000.0).pow(1.0 / 8.0)).roundToInt()
		val travelDistance = Math.toRadians(max(min(5, trackMovements / min(1, accelDistance)), 1).toDouble())

		// Get the orthogal angle to use as an axis to rotate the vector to the tow center to get an intermediate vector with a limited travel angle
		val orthogonal = vectorToTowCenter.clone().crossProduct(viewDirection).normalize()

		val newDirection = vectorToTowCenter.clone().rotateAroundAxis(orthogonal, travelDistance)

		// Project the new direction at the same distance from the player's eyes
		val newLocation = newDirection.clone().normalize().multiply(distanceToTowCenter).add(playerPilot.eyeLocation.toVector())

		// get the delta coordinates to move the towed structure
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

		private var lastRotation = System.nanoTime()

		override fun handleSwapHands(event: PlayerSwapHandItemsEvent) {
			if (event.offHandItem.type != StarshipControl.CONTROLLER_TYPE) return

			event.isCancelled = true

			val towedRotationTime = tractor?.getTowed()?.rotationTime ?: return
			val now = System.nanoTime()
			if (now - lastRotation < towedRotationTime) return
			lastRotation = now

			if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

			queuedRotations.add(90.0)
		}

		override fun handleDropItem(event: PlayerDropItemEvent) {
			if (event.itemDrop.itemStack.type != StarshipControl.CONTROLLER_TYPE) return

			event.isCancelled = true

			val towedRotationTime = tractor?.getTowed()?.rotationTime ?: return
			val now = System.nanoTime()
			if (now - lastRotation < towedRotationTime) return
			lastRotation = now

			if (event.player.hasCooldown(StarshipControl.CONTROLLER_TYPE)) return

			lastRotationAttempt[event.player.uniqueId] = System.currentTimeMillis()

			queuedRotations.add(270.0)
		}
	}
}
