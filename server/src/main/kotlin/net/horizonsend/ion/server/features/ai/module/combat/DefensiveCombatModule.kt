package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier

class DefensiveCombatModule(
	controller: AIController,
	difficulty: DifficultyModule,
	aiming : AimingModule,
	targetingSupplier: Supplier<AITarget?>
) : SingleTargetCombatModule(controller, difficulty,aiming,targetingSupplier) {
	override fun tick() {
		val target = targetingSupplier.get() ?: return

		handleAutoWeapons(getCenterVec3i(), target)
	}
}
