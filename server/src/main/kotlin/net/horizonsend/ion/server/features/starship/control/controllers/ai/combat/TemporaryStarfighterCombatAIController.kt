package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel

/**
 * This class is designed for an easy low block count opponent
 * Assumes:
 *  - No weapon sets
 *  - Forward only weaponry
 *
 * It does not use DC, only shift flies and cruises
 **/
class TemporaryStarfighterCombatAIController(
	starship: ActiveStarship,
	target: ActiveStarship?,
	aggressivenessLevel: AggressivenessLevel,
	override val previousController: AIController,
) : StarfighterCombatAIController(starship, target, aggressivenessLevel),
	TemporaryAIController {
	override fun tick() {
		if (target == null) returnToPreviousController()

		super.tick()
	}
}
