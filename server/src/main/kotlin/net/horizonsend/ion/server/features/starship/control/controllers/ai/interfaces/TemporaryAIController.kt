package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.IdleController

interface TemporaryAIController {
	val starship: ActiveStarship
	val previousController: Controller?

	fun defaultIdleController(controller: AIController): IdleController {
		val idleEndCondition: (IdleController) -> Boolean = {
			it.getNearbyShips(0.0, controller.aggressivenessLevel.engagementDistance, controller.nonAICheck).isNotEmpty()
		}

		return IdleController(controller, idleEndCondition)
	}

	fun returnToPreviousController() {
		if (this !is AIController) return

		val previousController = previousController ?: defaultIdleController(this)
		starship.controller = previousController
	}
}
