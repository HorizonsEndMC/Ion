package net.horizonsend.ion.server.features.starship.active.ai.module.movement

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.PathfindingModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import org.bukkit.Location
import org.bukkit.block.BlockFace
import java.util.function.Supplier
import kotlin.math.abs

/**
 * Basic cruise flight module.
 * Variable destination, it automatically flies towards it.
 *
 * @param shiftFlightType Condition on when to use shift flight
 * @param maximumCruiseDistanceSquared Does not cruise if the target is within this distance (squared).
 **/
class CruiseModule(
	controller: AIController,
	pathfindingModule: PathfindingModule,
	val cruiseDestination: Supplier<Vec3i?>,
	var shiftFlightType: ShiftFlightType,
	var maximumCruiseDistanceSquared: Double = 90000.0,
) : MovementModule(controller, pathfindingModule) {
	// The pathfinding controller will change the destination, so store the eventual destination in a seperate variable.
	var speedLimit = -1

	override fun tick() {
		(starship as ActiveControlledStarship).speedLimit = speedLimit

		val origin = starshipLocation.toLocation(world)

		Tasks.sync {
			if (assessDistance()) handleCruise()

			shiftFlightType.handleShiftFlight(this, origin)
			shiftFlightType.refresh(controller)
		}
	}

	fun handleCruise() {
		if (controller.hasBeenBlockedWithin()) {
			starship.debug("Blocked, stopping cruising")
			stopCruising(true)
			return
		}

		starship.debug("More than sqrt($maximumCruiseDistanceSquared) blocks away, cruising")
		cruiseToVec3i(starshipLocation, getDestination())
	}

	/** Returns true if the destination is sufficiently far that it should cruise */
	private fun assessDistance(): Boolean {
		val destination = getDestination()

		val distance = distanceSquared(destination.toVector(), getCenter().toVector())

		// 300 Blocks or more and it should cruise
		return distance >= maximumCruiseDistanceSquared // Don't bother with the sqrt
	}

	override fun shutDown() {
		stopCruising()
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		starship.debug("""
			Controller $controller

			is blocked: $reason

			by $movement

			trying to move to ${getDestination()}
		""".trimIndent())
	}

	enum class ShiftFlightType {
		NONE,
		MATCH_Y {
			override fun handleShiftFlight(module: CruiseModule, origin: Location) {
				val destination = module.getDestination()

				val difference = origin.y - destination.y
				if (abs(difference) < 5) {
					module.controller.setShiftFlying(false)
					return
				}

				val blockFace = if (difference > 0) BlockFace.UP else BlockFace.DOWN

				module.shiftFlyTowardsBlockFace(blockFace, false)
			}
		},
		IF_BLOCKED {
			override fun handleShiftFlight(module: CruiseModule, origin: Location) {
				val pathfindingModule = module.controller.modules["pathfinding"] as? PathfindingModule ?: return
				val blocked = (pathfindingModule as? PathfindingModule)?.blocked ?: module.controller.hasBeenBlockedWithin()

				if (!blocked) {
					module.controller.setShiftFlying(false)
					return
				}

				module.shiftFlyInDirection(pathfindingModule.getMovementVector(), true)
			}
		},
		IF_BLOCKED_AND_MATCH_Y {
			override fun handleShiftFlight(module: CruiseModule, origin: Location) {
				val destination = module.cruiseDestination.get() ?: return
				val pathfindingModule = module.controller.modules["pathfinding"] as? PathfindingModule ?: return
				val blocked = (pathfindingModule as? PathfindingModule)?.blocked ?: module.controller.hasBeenBlockedWithin()
				val yObjective = destination.y

				val yDifference = yObjective - origin.y

				// Only try to match y if not blocked
				if (abs(yDifference) > 10.0 && !blocked) {
					val blockFace = if (yDifference > 0) BlockFace.UP else BlockFace.DOWN
					module.shiftFlyTowardsBlockFace(blockFace, false)

					return
				}

				if (!blocked) {
					module.controller.setShiftFlying(false)
					return
				}

				module.shiftFlyInDirection(pathfindingModule.getMovementVector(), true)
			}
		},
		ALL {
			override fun handleShiftFlight(module: CruiseModule, origin: Location) {
				module.shiftFly(origin, false)
			}

			override fun refresh(controller: AIController) { }
		}

		;

		/** Must be executed sync */
		open fun handleShiftFlight(module: CruiseModule, origin: Location) {}
		open fun refresh(controller: AIController) {
			if (!controller.hasBeenBlockedWithin()) controller.setShiftFlying(false)
		}
	}

	override fun toString(): String = "CruiseModule[destination: ${cruiseDestination.get()}, shiftFlightType: $shiftFlightType]"
}
