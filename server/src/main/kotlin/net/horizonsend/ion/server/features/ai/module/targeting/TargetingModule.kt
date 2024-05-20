package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

abstract class TargetingModule(controller: AIController) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	/** Determines if the module should stick to a target once finding it */
	var sticky: Boolean = false
	var lastTarget: AITarget? = null

	open fun findTarget(): AITarget? {
		if (sticky && lastTarget != null) return lastTarget
		return searchForTarget()
	}

	protected abstract fun searchForTarget(): AITarget?

	override fun toString(): String {
		return "${javaClass.simpleName}[sticky: $sticky, lastTarget: $lastTarget]"
	}
}
