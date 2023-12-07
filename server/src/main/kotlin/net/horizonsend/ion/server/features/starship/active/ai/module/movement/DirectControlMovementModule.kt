package net.horizonsend.ion.server.features.starship.active.ai.module.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.AStarPathfindingModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

abstract class DirectControlMovementModule(controller: AIController, pathfindingModule: AStarPathfindingModule,) :
	MovementModule(controller, pathfindingModule) { //TODO

	init {
	    require(starship is ActiveControlledStarship)
	}

	override fun getCenterVec3i(): Vec3i {
		return Vec3i((starship as ActiveControlledStarship).data.blockKey)
	}

	override fun getCenter(): Location {
		return getCenterVec3i().toLocation(world)
	}

	override fun shutDown() {
		controller.selectedDirectControlSpeed = 1
		(starship as ActiveControlledStarship).setDirectControlEnabled(false)
	}
}
