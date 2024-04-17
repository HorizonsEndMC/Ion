package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class SingleTargetingModule(controller: AIController, val target: AITarget) : TargetingModule(controller) {
	override fun searchForTarget(): AITarget = target
}
