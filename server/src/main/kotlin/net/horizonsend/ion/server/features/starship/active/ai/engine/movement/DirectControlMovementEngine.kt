package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.AStarPathfindingEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

abstract class DirectControlMovementEngine(controller: ActiveAIController, pathfindingEngine: AStarPathfindingEngine,) :
	MovementEngine(controller, pathfindingEngine) { //TODO

	init {
	    require(starship is ActiveControlledStarship)
	}

	override fun getCenterVec3i(): Vec3i {
		starship as ActiveControlledStarship

		return Vec3i(starship.data.blockKey)
	}

	override fun getCenter(): Location {
		return getCenterVec3i().toLocation(world)
	}

	override fun shutDown() {
		starship as ActiveControlledStarship

		controller.selectedDirectControlSpeed = 1
		starship.setDirectControlEnabled(false)
	}
}
