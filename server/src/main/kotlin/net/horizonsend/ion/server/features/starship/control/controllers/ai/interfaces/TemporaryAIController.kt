package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

interface TemporaryAIController {
	val starship: ActiveStarship
	val previousController: Controller

	fun returnToPreviousController() {
		if (this !is AIController) return

		val previousController = previousController
		starship.controller = previousController
	}
}
