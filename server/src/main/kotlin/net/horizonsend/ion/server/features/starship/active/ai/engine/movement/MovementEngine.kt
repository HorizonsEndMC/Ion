package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

/** Controlling the movement of the starship */
abstract class MovementEngine(controller: AIController) : AIEngine(controller) {
	abstract var destination: Vec3i?
	val starshipLocation: Vec3i get() = getCenterVec3i()

	fun getVector(origin: Vector, destination: Vector, normalized: Boolean = false): Vector {
		val vec = destination.clone().subtract(origin)

		return if (normalized) vec.normalize() else vec
	}

	fun getDistance(origin: Vector, destination: Vector) = getVector(origin, destination, normalized = false).length()

	open fun shiftFly(
		origin: Location,
		stopCruising: Boolean = false
	) = Tasks.sync {
		val destination = this.destination

		AIControlUtils.shiftFlyToLocation(controller, starshipLocation, destination)
	}

	/** Faces the target */
	fun faceTarget(origin: Location) {
		val destination = destination ?: return
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
		val direction = destination?.toVector()?.let { getVector(origin.toVector(), it) } ?: return

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

		if (starship.cruiseData.targetDir == direction.normalize()) return

		Tasks.sync { StarshipCruising.startCruising(controller, starship, direction) }
	}

	fun stopCruising() {
		val starship = controller.starship as ActiveControlledStarship

		val isCruising = StarshipCruising.isCruising(starship)

		if (isCruising) StarshipCruising.stopCruising(controller, starship)
	}
}
