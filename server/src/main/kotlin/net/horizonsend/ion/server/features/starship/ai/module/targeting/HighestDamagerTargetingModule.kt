package net.horizonsend.ion.server.features.starship.ai.module.targeting

import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class HighestDamagerTargetingModule(controller: AIController) : TargetingModule(controller) {
	override fun searchForTarget(): AITarget? {
		return starship.damagers.maxByOrNull { it.value.points.get() }?.key?.getAITarget()
	}
}
