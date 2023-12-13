package net.horizonsend.ion.server.features.starship.active.ai.module.misc

import net.horizonsend.ion.server.features.starship.active.ai.module.combat.CombatModule
import net.horizonsend.ion.server.features.starship.active.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager

class AggroUponDamageModule(
	controller: AIController,
	private val combatModule: (AIController, TargetingModule) -> CombatModule
) : TargetingModule(controller) {
	var damagedBy: AITarget? = null

	override fun searchForTarget(): AITarget? {
		return damagedBy
	}

	override fun onDamaged(damager: Damager) {
		val combatController = controller.modules["combat"] as? CombatModule

		if (combatController == null) controller.modules["combat"] = combatModule(controller, this)

		if (damagedBy != null) return

		damagedBy = damager.getAITarget()
	}
}
