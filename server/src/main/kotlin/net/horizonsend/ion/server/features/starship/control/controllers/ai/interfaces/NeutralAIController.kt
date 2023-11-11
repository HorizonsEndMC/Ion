package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIControllers
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

/**
 * Neutral in the minecraft sense.
 * A controller that will switch to an aggressive controller
 **/
interface NeutralAIController : AggressiveLevelAIController {
	val starship: ActiveStarship
	val combatFactory: AIControllers.AIControllerFactory<*>

	fun createCombatController(controller: AIController, target: AITarget): AIController {
		this as AIController

		return combatFactory.createController(
			starship,
			pilotName,
			target,
			null,
			aggressivenessLevel,
			mutableSetOf(),
			mutableSetOf(),
			this
		)
	}

	fun combatMode(controller: AIController, target: AITarget?) {
		target ?: return
		val combatMode = createCombatController(controller, target)

		starship.controller = combatMode
	}
}
