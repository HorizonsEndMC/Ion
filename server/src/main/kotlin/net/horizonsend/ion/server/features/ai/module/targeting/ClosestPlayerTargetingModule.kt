package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class ClosestPlayerTargetingModule(
	controller: AIController,
	var maxRange: Double,
) : TargetingModule(controller) {
	override fun searchForTarget(): AITarget? {
		return searchForTargetList().firstOrNull()
	}

	override fun searchForTargetList(): List<AITarget> {
		return controller.getNearbyTargetsInRadius(0.0, 0.0, playerRange = maxRange) {
			targetFilter(it, false)
		}.toList()
	}
}
