package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController

class TemporaryFrigateCombatController(
	override val previousController: AIController,
	target: AITarget,
	manualWeaponSets: Set<WeaponSet>,
	autoWeaponSets: Set<WeaponSet>
) : FrigateCombatAIController(
	previousController.starship,
	target,
	previousController.pilotName,
	previousController.aggressivenessLevel,
	manualWeaponSets,
	autoWeaponSets
), TemporaryAIController {
	override fun tick() {
		if (target == null) returnToPreviousController()

		super.tick()
	}
}
