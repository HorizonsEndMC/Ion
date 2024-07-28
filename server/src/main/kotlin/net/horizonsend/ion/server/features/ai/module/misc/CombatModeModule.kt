package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.AIControllerFactory
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

/**
 * Switches to a combat mode when it detects a nearby ship that meets the provided criteria
 **/
class CombatModeModule(
	controller: AIController,
	private val combatController: AIControllerFactory,
	private val minRange: Double,
	private val targetFilter: (AITarget) -> Boolean
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	override fun tick() {
		val target = controller.getNearbyTargetsInRadius(0.0, minRange, minRange, targetFilter).firstOrNull() ?: return

		switchToCombatMode(target)
	}

	private fun switchToCombatMode(target: AITarget) {
		val combatController = combatController(
			starship,
			controller.getPilotName(),
			controller.getAllManualSets(),
			controller.getAllAutoSets()
		)

		starship.setController(combatController)
	}
}
