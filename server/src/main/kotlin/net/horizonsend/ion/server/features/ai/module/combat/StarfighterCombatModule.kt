package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import java.util.function.Supplier

class StarfighterCombatModule(
	controller: AIController,
	difficulty : DifficultyModule,
	aiming : AimingModule,
	targetingSupplier: Supplier<AITarget?>
) : SingleTargetCombatModule(controller,difficulty,aiming, targetingSupplier) {
	override var shouldFaceTarget: Boolean = true

	override fun tick() {
		val target = targetingSupplier.get() ?: return

		val distance = target.getLocation().toVector().distance(getCenter().toVector())
		if (distance > 750) {return}

		handleAutoWeapons(starship.centerOfMass, target)
		fireAllWeapons(
			origin = starship.centerOfMass,
			target = target,
			false
		)
	}
}
