package net.horizonsend.ion.server.features.starship.active.ai.module.targeting

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class ClosestTargetingModule(
	controller: AIController,
	val maxRange: Double,
	existingTarget: AITarget? = null
) : TargetingModule(controller) {

	init {
		lastTarget = existingTarget
	}

	override fun searchForTarget(): AITarget? {
		return controller.getNearbyTargetsInRadius(0.0, maxRange) { if (it is StarshipTarget) { it.ship.controller !is AIController } else true }.firstOrNull()
	}
}
