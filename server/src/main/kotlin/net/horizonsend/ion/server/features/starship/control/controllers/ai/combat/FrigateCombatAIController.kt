package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.CruiseEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindIfBlockedEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.AxisStandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AIStarshipTemplates
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.CombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
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
	override var target: AITarget?,
	aggressivenessLevel: AggressivenessLevel,
	override val manualWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>,
	override val autoWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>
): ActiveAIController(starship, "FrigateCombatMatrix", AIShipDamager(starship), aggressivenessLevel),
	CombatAIController {
	override val pathfindingEngine: PathfindingEngine = PathfindIfBlockedEngine(this, target?.getVec3i())
	override val movementEngine: MovementEngine = CruiseEngine(this, target?.getVec3i(), CruiseEngine.ShiftFlightType.ALL)
	override val positioningEngine = AxisStandoffPositioningEngine(this, target, 240.0)

	override var locationObjective: Location? = target?.let { it.getLocation() }

	val lastBlockedTime get() = (starship as ActiveControlledStarship).lastBlockedTime

	override fun tick() {
		super.tick()

		if (target == null) aggressivenessLevel.findNextTarget(this)
		val target = this.target ?: return

		val ok = checkOnTarget()

		if (!ok) aggressivenessLevel.disengage(this)

//		positioningEngine.target = target.centerOfMass
		tickAll()
	}

	/**
	 * Gets information about the target, updates the immediate navigation goal
	 *
	 * If target has moved out of range, deals with that scenario
	 **/
	private fun checkOnTarget(): Boolean {
		val target = this.target ?: return false

		val location = getCenter()
		val targetLocation = target.getVec3i().toVector()

		if (!target.isActive()) return false

		if (target.getWorld() != starship.world) {
			// If null, they've likely jumped to hyperspace, disengage
			val planet = Space.planetWorldCache[target.getWorld()].getOrNull() ?: return false

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
					locationObjective = targetLocation.toLocation(target.getWorld())
					return true
				}

				false
			}

			// They are getting far away so focus on moving towards them
			(distance in 500.0..aggressivenessLevel.engagementDistance) -> {
				locationObjective = targetLocation.toLocation(target.getWorld())
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
