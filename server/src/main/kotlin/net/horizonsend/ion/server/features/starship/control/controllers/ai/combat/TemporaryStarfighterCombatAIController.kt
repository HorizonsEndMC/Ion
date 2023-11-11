package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController

/**
 * This class is designed for an easy low block count opponent
 * Assumes:
 *  - No weapon sets
 *  - Forward only weaponry
 *
 * It does not use DC, only shift flies and cruises
 **/
class TemporaryStarfighterCombatAIController(
	target: AITarget?,
	override val previousController: AIController,
) : StarfighterCombatAIController(previousController.starship, target, previousController.pilotName, previousController.aggressivenessLevel),
	TemporaryAIController {
	override fun tick() {
		if (target == null) returnToPreviousController()

		super.tick()
	}
}
