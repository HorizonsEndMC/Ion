package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

/** Controlling the movement of the starship */
abstract class MovementEngine(
	controller: ActiveAIController,
	val pathfindingEngine: PathfindingEngine
) : AIEngine(controller) {
	val starshipLocation: Vec3i get() = getCenterVec3i()

	fun getVector(origin: Vector, destination: Vector, normalized: Boolean = false): Vector {
		val vec = destination.clone().subtract(origin)

		return if (normalized) vec.normalize() else vec
	}

	fun getDistance(origin: Vector, destination: Vector) = getVector(origin, destination, normalized = false).length()

	open fun getDestination(): Vec3i {
		val pathfindingObjective = pathfindingEngine.getFirstNavPoint()
		val destination = pathfindingEngine.getFallbackPosition()

		// if target is more than 22 blocks away give the pathfinding objective, else the destination
		return if (distanceSquared(destination, starshipLocation) <= 512) destination else pathfindingObjective
	}

	open fun shiftFly(
		origin: Location,
		stopCruising: Boolean = false
	) = Tasks.sync {
		val starship = controller.starship as ActiveControlledStarship
		if (stopCruising) StarshipCruising.stopCruising(controller, starship)

		val destination = getDestination()

		// If within 9 blocks of destination, don't bother moving
		if (distanceSquared(destination, starshipLocation) <= 81) return@sync

		AIControlUtils.shiftFlyToLocation(controller, starshipLocation, destination)
	}

	open fun shiftFlyTowardsBlockFace(
		blockFace: BlockFace,
		stopCruising: Boolean = false
	) = Tasks.sync {
		val starship = controller.starship as ActiveControlledStarship
		if (stopCruising) StarshipCruising.stopCruising(controller, starship)

		AIControlUtils.shiftFlyInDirection(controller, blockFace.direction)
	}

	open fun shiftFlyInDirection(
		direction: Vector,
		stopCruising: Boolean = false
	) = Tasks.sync {
		val starship = controller.starship as ActiveControlledStarship
		if (stopCruising) StarshipCruising.stopCruising(controller, starship)

		AIControlUtils.shiftFlyInDirection(controller, direction)
	}

	open fun shiftFlyToVec3i(
		origin: Location,
		destination: Vec3i?,
		stopCruising: Boolean = false
	) = Tasks.sync {
		val starship = controller.starship as ActiveControlledStarship
		if (stopCruising) StarshipCruising.forceStopCruising(starship)

		AIControlUtils.shiftFlyToLocation(controller, Vec3i(origin), destination)
	}

	/** Faces the target */
	fun faceTarget(origin: Location) {
		val destination = getDestination()
		val direction = getVector(origin.toVector(), destination.toVector())

		faceDirection(direction)
	}

	/** Faces the provided target */
	fun faceTarget(origin: Location, target: Vector) {
		val direction = getVector(origin.toVector(), target)

		faceDirection(direction)
	}

	/** Faces a vector */
	fun faceDirection(vectorToTarget: Vector) {
		val blockFace = vectorToBlockFace(vectorToTarget)
		AIControlUtils.faceDirection(controller, blockFace)
	}

	/** Faces a blockface */
	fun faceDirection(blockFace: BlockFace) {
		AIControlUtils.faceDirection(controller, blockFace)
	}

	fun cruiseForward() = Tasks.sync {
		val starship = controller.starship as ActiveControlledStarship
		StarshipCruising.startCruising(controller, starship, starship.forward.direction)
	}

	fun cruiseForward(diagonal: StarshipCruising.Diagonal) = Tasks.sync {
		val starship = controller.starship as ActiveControlledStarship
		StarshipCruising.startCruising(controller, starship, diagonal.vector(starship.forward))
	}

	fun cruiseToDestination(origin: Location) {
		val direction = getVector(origin.toVector(), getDestination().toVector())

		cruiseInDirection(direction)
	}

	fun cruiseToVec3i(starshipCenter: Vec3i, vec3i: Vec3i) {
		val direction = getVector(starshipCenter.toVector(), vec3i.toVector())

		cruiseInDirection(direction)
	}

	fun cruiseInDirection(direction: Vector) {
		val starship = controller.starship as ActiveControlledStarship
		val facing = starship.forward

		val blockFace = vectorToBlockFace(direction)

		if (facing != blockFace) {
			Tasks.sync { AIControlUtils.faceDirection(controller, blockFace) }

			// Can't cruise if not facing the right direction
			return
		}

		Tasks.sync { StarshipCruising.startCruising(controller, starship, direction) }
	}

	fun stopCruising(immediate: Boolean = false) {
		val starship = controller.starship as ActiveControlledStarship

		val isCruising = StarshipCruising.isCruising(starship)

		if (isCruising) if (immediate) StarshipCruising.forceStopCruising(starship) else StarshipCruising.stopCruising(controller, starship)
	}
}
