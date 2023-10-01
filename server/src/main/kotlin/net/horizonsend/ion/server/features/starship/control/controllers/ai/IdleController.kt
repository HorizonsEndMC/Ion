package net.horizonsend.ion.server.features.starship.control.controllers.ai

/** Idles until a condition is met */
class IdleController(
	private val previousController: AIController,
	val returnCondition: (IdleController) -> Boolean
) : AIController(previousController.starship, "combat", previousController.aggressivenessLevel) {
	override fun tick() {
		if (!returnCondition(this)) return

		starship.controller = previousController
	}
}
