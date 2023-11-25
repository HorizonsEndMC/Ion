package net.horizonsend.ion.server.features.starship.active.ai.engine.targeting

import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

abstract class TargetingEngine(controller: AIController) : AIEngine(controller) {
	/** Determines if the engine should stick to a target once finding it */
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
