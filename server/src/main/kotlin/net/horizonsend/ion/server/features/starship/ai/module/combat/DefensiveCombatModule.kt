package net.horizonsend.ion.server.features.starship.ai.module.combat

import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier

class DefensiveCombatModule(controller: AIController, targetingSupplier: Supplier<AITarget?>) : CombatModule(controller, targetingSupplier) {
	override fun tick() {
		val target = targetingSupplier.get() ?: return

		handleAutoWeapons(getCenterVec3i(), target)
	}
}
