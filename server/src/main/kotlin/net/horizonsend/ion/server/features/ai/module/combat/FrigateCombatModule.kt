package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier

class FrigateCombatModule(
	controller: AIController,
	difficulty: DifficultyModule,
	private val toggleRandomTargeting: Boolean = true,
	aiming: AimingModule,
	targetingSupplier: Supplier<AITarget?>
) : SingleTargetCombatModule(controller, difficulty, aiming, targetingSupplier) {
	var leftFace: Boolean = false
	var ticks = 0
	private var aimAtRandom = false

	override fun tick() {
		ticks++
		val target = targetingSupplier.get() ?: return

		val distance = target.getLocation().toVector().distance(getCenter().toVector())
		if (distance > 750) {
			return
		}

		handleAutoWeapons(starship.centerOfMass, target)
		fireAllWeapons(
			origin = starship.centerOfMass,
			target = target,
			aimAtRandom
		)

		if (toggleRandomTargeting && ticks % 40 == 0) {
			aimAtRandom = !aimAtRandom
		}
	}
}
