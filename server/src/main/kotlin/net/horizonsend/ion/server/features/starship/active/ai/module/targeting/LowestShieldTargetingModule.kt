package net.horizonsend.ion.server.features.starship.active.ai.module.targeting

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class LowestShieldTargetingModule(controller: AIController,	existingTarget: AITarget? = null) : TargetingModule(controller) {
	init {
		lastTarget = existingTarget
	}

	override fun searchForTarget(): AITarget? {
		TODO("Not yet implemented")
	}
}
