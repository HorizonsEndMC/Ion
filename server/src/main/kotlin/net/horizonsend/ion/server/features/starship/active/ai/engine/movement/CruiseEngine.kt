package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared

/**
 * Basic cruise flight engine.
 * Variable destination, it automatically flies towards it.
 *
 * @param useShiftFlightForPrecision Whether to also shift fly while cruising.
 * @param maximumCruiseDistanceSquared Does not cruise if the target is within this distance.
 **/
class CruiseEngine(
	controller: AIController,
	override var destination: Vec3i?,
	var useShiftFlightForPrecision: Boolean = true,
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
			if (assessDistance()) {
				faceTarget(origin)
				cruiseToVec3i(starshipLocation, cruiseDestination ?: return@sync)
			}

			if (useShiftFlightForPrecision) shiftFly(origin, false)
		}
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
}
