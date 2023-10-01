package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIStarshipTemplates
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.CombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.NavigationEngine
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

class FrigateCombatController(
	starship: ActiveStarship,
	override var target: ActiveStarship?,
	aggressivenessLevel: AggressivenessLevel,
	override val manualWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>,
	override val autoWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>,
	private val previousController: AIController?
): AIController(starship, "FrigateCombatMatrix", aggressivenessLevel),
	CombatController {
	val navigationEngine: NavigationEngine = NavigationEngine(this, target?.centerOfMass).apply {
		shouldRotateDuringShiftFlight = true
	}

	override var locationObjective: Location? = target?.let { it.centerOfMass.toLocation(it.world) }
	override fun getTargetLocation(): Location? {
		TODO("Not yet implemented")
	}

	override fun getObjective(): Vec3i? {
		TODO("Not yet implemented")
	}


}
