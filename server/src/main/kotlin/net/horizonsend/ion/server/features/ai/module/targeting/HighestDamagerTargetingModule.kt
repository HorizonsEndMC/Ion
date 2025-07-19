package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class HighestDamagerTargetingModule(
	controller: AIController,
	targetAI : Boolean = false,) : TargetingModule(controller, targetAI) {
	override fun searchForTarget(): AITarget? {
		return starship.damagers.maxByOrNull { it.value.points.get() }?.key?.getAITarget()
	}

	override fun searchForTargetList(): List<AITarget> {
		return starship.damagers.keys.mapNotNull { it.getAITarget() }
	}
}
