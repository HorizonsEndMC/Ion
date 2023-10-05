package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

/**
 * Neutral in the minecraft sense.
 * A controller that will switch to an aggressive controller
 **/
interface NeutralAIController : AggressiveLevelAIController {
	val starship: ActiveStarship

	fun createCombatController(controller: AIController, target: ActiveStarship): AIController

	fun combatMode(controller: AIController, target: ActiveStarship) {
		val combatMode = createCombatController(controller, target)

		starship.controller = combatMode
	}
}
