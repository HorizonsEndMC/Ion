package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

abstract class DirectControlMovementEngine(controller: AIController) : MovementEngine(controller) { //TODO
	override var destination: Vec3i? = null

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
