package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import co.aikar.commands.ConditionFailedException
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import org.bukkit.Location
import org.bukkit.block.BlockFace
import kotlin.math.abs

/**
 * Basic cruise flight engine.
 * Variable destination, it automatically flies towards it.
 *
 * @param shiftFlightType Condition on when to use shift flight
 * @param maximumCruiseDistanceSquared Does not cruise if the target is within this distance.
 **/
class CruiseEngine(
	controller: ActiveAIController,
	override var destination: Vec3i?,
	var shiftFlightType: ShiftFlightType,
	var maximumCruiseDistanceSquared: Double = 90000.0,
) : MovementEngine(controller) {
	// The pathfinding controller will change the destination, so store the eventual destination in a seperate variable.
	var cruiseDestination = destination
	var speedLimit = -1

	override fun tick() {
		starship as ActiveControlledStarship
		starship.speedLimit = speedLimit

		val origin = starshipLocation.toLocation(world)

		Tasks.sync {
			if (assessDistance()) handleCruise(origin)

			shiftFlightType.handleShiftFlight(this, origin)
			shiftFlightType.refresh(controller)
		}
	}

	fun handleCruise(origin: Location) {
		if (controller.blocked) {
			stopCruising(true)
			return
		}

		val destination = cruiseDestination?.toVector() ?: return

		val distanceSquared = distanceSquared(origin.toVector(), destination)

		if (distanceSquared >= 250000) {
			faceTarget(origin)
			cruiseToVec3i(starshipLocation, cruiseDestination ?: return)

			return
		}

		stopCruising()
	}

	/** Returns true if the destination is sufficiently far that it should cruise */
	private fun assessDistance(): Boolean {
		val destination = destination ?: return true

		val distance = distanceSquared(destination.toVector(), getCenter().toVector())

		// 300 Blocks or more and it should cruise
		return distance >= maximumCruiseDistanceSquared // Don't bother with the sqrt
	}

	override fun shutDown() {
		stopCruising()
	}

	override fun onBlocked(movement: StarshipMovement, reason: ConditionFailedException) {
		debugAudience.debug("$controller is blocked $reason by movement $movement trying to move to $destination eventual destination $cruiseDestination")
	}

	enum class ShiftFlightType {
		NONE,
		MATCH_Y {
			override fun handleShiftFlight(engine: CruiseEngine, origin: Location) {
				val destination = engine.destination ?: return

				val difference = origin.y - destination.y
				if (abs(difference) < 5) return

				val blockFace = if (difference > 0) BlockFace.UP else BlockFace.DOWN

				engine.shiftFlyTowardsBlockFace(blockFace, false)
			}
		},
		IF_BLOCKED {
			override fun handleShiftFlight(engine: CruiseEngine, origin: Location) {
				if (engine.controller.blocked) return

				engine.shiftFlyToVec3i(origin, (engine.controller as ActiveAIController).pathfindingEngine.getFirstNavPoint(), true)
			}
		},
		IF_BLOCKED_AND_MATCH_Y {
			override fun handleShiftFlight(engine: CruiseEngine, origin: Location) {
				// Null for true, false for match y, true for blocked
				var shouldFly: Boolean? = null
				val destination = engine.destination ?: return

				val difference = origin.y - destination.y
				if (abs(difference) >= 5) shouldFly = false

				if (engine.controller.blocked) shouldFly = true

				if (shouldFly == null) return

				if (!shouldFly) {
					engine.shiftFly(origin, false)
				} else {
					engine.shiftFlyToVec3i(origin, (engine.controller as ActiveAIController).pathfindingEngine.getFirstNavPoint(), true)
				}
			}
		},
		ALL {
			override fun handleShiftFlight(engine: CruiseEngine, origin: Location) {
				engine.shiftFly(origin, false)
			}

			override fun refresh(controller: AIController) { }
		}

		;

		/** Must be executed sync */
		open fun handleShiftFlight(engine: CruiseEngine, origin: Location) {}
		open fun refresh(controller: AIController) {
			if (!controller.blocked) controller.isShiftFlying = false
		}
	}
}
