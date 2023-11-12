package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.ShiftFlightMovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.CombatAStarPathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.RotatingAxisStandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.CombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

/**
 * This class is designed for an easy low block count opponent
 * Assumes:
 *  - No weapon sets
 *  - Forward only weaponry
 *
 * It does not use DC, only shift flies and cruises
 **/
open class StarfighterCombatAIController(
	starship: ActiveStarship,
	final override var target: AITarget?,
	pilotName: Component?,
	aggressivenessLevel: AggressivenessLevel
) : ActiveAIController(starship, "StarfighterCombatMatrix", AIShipDamager(starship), pilotName, aggressivenessLevel),
	CombatAIController {
	final override var positioningEngine: RotatingAxisStandoffPositioningEngine = RotatingAxisStandoffPositioningEngine(this, target,  25.0, CARDINAL_BLOCK_FACES.toList())
	final override var pathfindingEngine = CombatAStarPathfindingEngine(this, positioningEngine)
	final override var movementEngine: MovementEngine = ShiftFlightMovementEngine(this, pathfindingEngine)

	override val autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf()
	override val manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf()

	override fun destroy() {
		shutDownAll()
		super.destroy()
	}

	override var locationObjective: Location? = target?.getLocation()

	/** Current state of the AI */
	var state: State = State.FOCUS_LOCATION

	enum class State {
		COMBAT, /** Focus on the combat loop */
		FOCUS_LOCATION, /** Only worry about moving towards the location objective */
	}

	/**
	 * Function to find the standoff distance when in combat
	 *
	 * Based on shield health
	 **/
	private fun getStandoffDistance(target: ActiveStarship) : Double {
		val min = 25.0

		val shieldMultiplier = averageHealth

		val disengageMultiplier = (0.1 / aggressivenessLevel.disengageMultiplier) * shieldMultiplier

		val blockCountMultiplier = target.initialBlockCount.toDouble().pow(1.0/3.0) / 10

		return min + ((1 / disengageMultiplier) * blockCountMultiplier)
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
				state = State.FOCUS_LOCATION

				locationObjective = planet.location.toLocation(planet.spaceWorld!!)
				return true
			}

			// Don't follow to planet if low aggressiveness
			return false
		}

		val distance = distance(location.toVector(), targetLocationVector)

		return when {
			// Check if they've moved out of range
			(distance > aggressivenessLevel.engagementDistance) -> {
				// Keep pursuing if aggressive, else out of range and should disengage
				if (aggressivenessLevel.ordinal >= AggressivenessLevel.HIGH.ordinal) {
					locationObjective = targetLocation
					state = State.FOCUS_LOCATION
					return true
				}

				false
			}

			// They are getting far away so focus on moving towards them
			(distance in 500.0..aggressivenessLevel.engagementDistance) -> {
				locationObjective = targetLocation
				state = State.FOCUS_LOCATION
				true
			}

			// They are in range, in the same world, should continue to engage
			else -> {
				// The combat loop will handle the location gathering
				state = State.COMBAT
				locationObjective = targetLocation
				true
			}
		}
	}

	/**
	 * Goals of this AI:
	 *
	 * Position itself at a standoff distance along a cardinal direction from the target
	 * This will allow it to engage with limited firing arc weaponry
	 *
	 * If no target is found, it will transition into a passive state
	 */

	override fun tick() {
		val ok = checkOnTarget()

		if (target == null) aggressivenessLevel.findNextTarget(this)
		val target = this.target ?: return
		positioningEngine.target = target

		if (!ok) {
			aggressivenessLevel.disengage(this)
			return
		}

		if (target is StarshipTarget) {
			positioningEngine.standoffDistance = getStandoffDistance(target.ship)
		} else {
			positioningEngine.standoffDistance = 25.0
		}
		tickAll()

		handleAutoWeapons(starship.centerOfMass)
		if (state == State.COMBAT) combatLoop()
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
