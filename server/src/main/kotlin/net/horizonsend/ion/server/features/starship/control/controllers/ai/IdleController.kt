package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController

/** Idles until a condition is met */
class IdleController(
	override val previousController: AIController,
	val returnCondition: (IdleController) -> Boolean
) : AIController(
	previousController.starship,
	"combat",
	previousController.damager,
	previousController.aggressivenessLevel
),
	TemporaryAIController {
	override fun tick() {
		if (returnCondition(this)) returnToPreviousController()
		super.tick()
	}
}
