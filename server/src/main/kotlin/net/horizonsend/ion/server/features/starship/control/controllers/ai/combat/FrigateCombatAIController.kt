package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.CruiseEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindIfBlockedEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.AxisStandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.CombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.Location
import org.bukkit.util.Vector
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
	override val manualWeaponSets: MutableList<WeaponSet>,
	override val autoWeaponSets: MutableList<WeaponSet>
): ActiveAIController(starship, "FrigateCombatMatrix", AIShipDamager(starship), aggressivenessLevel),
	CombatAIController {
	override val positioningEngine = AxisStandoffPositioningEngine(this, target, 240.0)
	override val pathfindingEngine: PathfindingEngine = PathfindIfBlockedEngine(this, positioningEngine)
	override val movementEngine = CruiseEngine(this, pathfindingEngine, target?.getVec3i(), CruiseEngine.ShiftFlightType.ALL)

	override var locationObjective: Location? = target?.getLocation()

	override fun tick() {
		if (target == null) aggressivenessLevel.findNextTarget(this)
		this.target ?: return

		val ok = checkOnTarget()

		if (!ok) aggressivenessLevel.disengage(this)

		tickAll()
		movementEngine.cruiseDestination = target?.getVec3i()
		handleAutoWeapons(starship.centerOfMass)
	}

	/**
	 * Gets information about the target, updates the immediate navigation goal
	 *
	 * If target has moved out of range, deals with that scenario
	 **/
	private fun checkOnTarget(): Boolean {
		val target = this.target ?: return false

		val location = getCenter()
		val targetLocationVector = target.getVec3i().toVector()
		val targetLocation = target.getLocation()

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

		val distance = distance(location.toVector(), targetLocationVector)
		locationObjective = targetLocation

		if (distance > aggressivenessLevel.engagementDistance) {
			return aggressivenessLevel.ordinal >= AggressivenessLevel.HIGH.ordinal
		}

		return true
	}

	private fun combatLoop() {
		val target = this.target ?: return

		// Get the closest axis
		(starship as ActiveControlledStarship).speedLimit = -1

		val faceDirection = vectorToBlockFace(getDirection(Vec3i(getCenter()), target.getVec3i(true)), includeVertical = false)

		fireAllWeapons(
			starship.centerOfMass,
			target.getVec3i(true),
			faceDirection = faceDirection
		) { direction ->
			if (aggressivenessLevel.shotDeviation > 0) {
				val offsetX = randomDouble(-aggressivenessLevel.shotDeviation, aggressivenessLevel.shotDeviation)
				val offsetY = randomDouble(-aggressivenessLevel.shotDeviation, aggressivenessLevel.shotDeviation)
				val offsetZ = randomDouble(-aggressivenessLevel.shotDeviation, aggressivenessLevel.shotDeviation)

				direction.add(Vector(offsetX, offsetY, offsetZ)).normalize()
			}
		}
	}
}
