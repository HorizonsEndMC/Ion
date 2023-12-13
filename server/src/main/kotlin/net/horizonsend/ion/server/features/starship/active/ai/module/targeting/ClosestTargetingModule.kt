package net.horizonsend.ion.server.features.starship.active.ai.module.targeting

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager

class ClosestTargetingModule(
	controller: AIController,
	val maxRange: Double,
	existingTarget: AITarget? = null
) : TargetingModule(controller) {
	private var lastDamaged: Long = 0

	init {
		lastTarget = existingTarget
	}

	override fun onDamaged(damager: Damager) {
		lastDamaged = System.currentTimeMillis()
		if (lastTarget == null) lastTarget = damager.getAITarget()
	}

	override fun searchForTarget(): AITarget? {
		if (lastTarget != null && lastDamaged >= System.currentTimeMillis() - 5000) return lastTarget

		return controller.getNearbyTargetsInRadius(0.0, maxRange) { if (it is StarshipTarget) { it.ship.controller !is AIController } else true }.firstOrNull()
	}
}
