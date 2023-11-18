package net.horizonsend.ion.server.features.starship.active.ai.engine.misc.targeting

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class HighestDamagerTargetingEngine(controller: AIController, existingTarget: AITarget? = null) : TargetingEngine(controller) {
	init {
		lastTarget = existingTarget
	}

	override fun searchForTarget(): AITarget? {
		TODO("Not yet implemented")
	}
}
