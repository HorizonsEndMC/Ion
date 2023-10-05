package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AIStarshipTemplates
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.CombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.miscellaneous.utils.distance
import org.bukkit.Location
import kotlin.jvm.optionals.getOrNull

/**
 * Weapons:
 *  Always fire at target with HTs
 *  Use LTs on ships <2000
 *
 * Heavy Weapons :
 *  Use TTs above 250m
 *  Use phasers below 250m
 *
 * Movement:
 *  Always cruise
 *  Orbit target at 240m
 *  Only use pathfinding if obstructed
 **/
class FrigateCombatAIController(
	starship: ActiveStarship,
	override var target: ActiveStarship?,
	aggressivenessLevel: AggressivenessLevel,
	override val manualWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>,
	override val autoWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>,
	override val previousController: AIController?
): AIController(starship, "FrigateCombatMatrix", aggressivenessLevel),
	CombatAIController,
	TemporaryAIController {
	val pathfindingEngine: PathfindingEngine = PathfindingEngine(this, target?.centerOfMass)

	override var locationObjective: Location = target?.let { it.centerOfMass.toLocation(it.world) }

	val lastBlockedTime get() = (starship as ActiveControlledStarship).lastBlockedTime

	override fun tick() {
		super.tick()

		val ok = checkOnTarget()

		if (!ok) aggressivenessLevel.disengage(this)
	}

	/**
	 * Gets information about the target, updates the immediate navigation goal
	 *
	 * If target has moved out of range, deals with that scenario
	 **/
	private fun checkOnTarget(): Boolean {
		val target = this.target ?: return false

		val location = getCenter()
		val targetLocation = target.centerOfMass.toVector()

		if (!ActiveStarships.isActive(target)) return false

		if (target.world != starship.world) {
			// If null, they've likely jumped to hyperspace, disengage
			val planet = Space.planetWorldCache[target.world].getOrNull() ?: return false

			// Only if it is very aggressive, follow the target to the planet they entered
			if (aggressivenessLevel.ordinal >= AggressivenessLevel.HIGH.ordinal) {

				locationObjective = planet.location.toLocation(planet.spaceWorld!!)
				return true
			}

			// Don't follow to planet if low aggressiveness
			return false
		}

		val distance = distance(location.toVector(), targetLocation)

		return when {
			// Check if they've moved out of range
			(distance > aggressivenessLevel.engagementDistance) -> {

				// Keep pursuing if aggressive, else out of range and should disengage
				if (aggressivenessLevel.ordinal >= AggressivenessLevel.HIGH.ordinal) {
					locationObjective = targetLocation.toLocation(target.world)
					return true
				}

				false
			}

			// They are getting far away so focus on moving towards them
			(distance in 500.0..aggressivenessLevel.engagementDistance) -> {
				locationObjective = targetLocation.toLocation(target.world)
				true
			}

			// They are in range, in the same world, should continue to engage
			else -> {
				// The combat loop will handle the location gathering
				true
			}
		}
	}
}
