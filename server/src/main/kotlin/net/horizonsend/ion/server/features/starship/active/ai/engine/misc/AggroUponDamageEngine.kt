package net.horizonsend.ion.server.features.starship.active.ai.engine.misc

import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.combat.CombatEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.targeting.TargetingEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager

class AggroUponDamageEngine(controller: AIController, val combatEngine: CombatEngine, existingTarget: AITarget? = null) : TargetingEngine(controller) {
	var damagedBy: AITarget? = null
	init {
		lastTarget = existingTarget
	}

	override fun searchForTarget(): AITarget? {
		return damagedBy
	}

	override fun onDamaged(damager: Damager) {
		val combatController = controller.engines["combat"] as? CombatEngine

		if (combatController == null) controller.engines["combat"] = combatEngine

		if (damagedBy != null) return

		damagedBy = damager.getAITarget()
	}
}
