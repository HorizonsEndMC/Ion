package net.horizonsend.ion.server.features.starship.control.controllers.ai.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.ShiftFlightMovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.AxisStandoffPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AIStarshipTemplates
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.CombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.TemporaryAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
class StarfighterCombatAIController(
	starship: ActiveStarship,
	override var target: ActiveStarship?,
	aggressivenessLevel: AggressivenessLevel,
	override val previousController: AIController?
) : AIController(starship, "StarfighterCombatMatrix", aggressivenessLevel),
	CombatAIController,
	TemporaryAIController,
	ActiveAIController {
	override val positioningEngine = AxisStandoffPositioningEngine(this, target, target?.let { getStandoffDistance(it) } ?: 25.0)
	override val pathfindingEngine: PathfindingEngine = PathfindingEngine(this, target?.centerOfMass)
	override val movementEngine: MovementEngine = ShiftFlightMovementEngine(this, target?.centerOfMass)

	override val autoWeaponSets: MutableList<AIStarshipTemplates.WeaponSet> = mutableListOf()
	override val manualWeaponSets: MutableList<AIStarshipTemplates.WeaponSet> = mutableListOf()

	override fun destroy() {
		pathfindingEngine.shutDown()
		super.destroy()
	}

	override val pilotName: Component get() = text()
		.append(text("Small Craft Combat Intelligence", NamedTextColor.GRAY))
		.append(text(" "))
		.append(aggressivenessLevel.displayName)
		.build()

	override var locationObjective: Location? = target?.let { it.centerOfMass.toLocation(it.world) }

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
		val targetLocation = target.centerOfMass.toVector()

		if (!ActiveStarships.isActive(target)) return false

		if (target.world != starship.world) {
			// If null, they've likely jumped to hyperspace, disengage
			val planet = Space.planetWorldCache[target.world].getOrNull() ?: return false

			// Only if it is very aggressive, follow the target to the planet they entered
			if (aggressivenessLevel.ordinal >= AggressivenessLevel.HIGH.ordinal) {
				state = State.FOCUS_LOCATION

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
					state = State.FOCUS_LOCATION
					return true
				}

				false
			}

			// They are getting far away so focus on moving towards them
			(distance in 500.0..aggressivenessLevel.engagementDistance) -> {
				locationObjective = targetLocation.toLocation(target.world)
				state = State.FOCUS_LOCATION
				true
			}

			// They are in range, in the same world, should continue to engage
			else -> {
				// The combat loop will handle the location gathering
				state = State.COMBAT
				true
			}
		}
	}

	override fun getObjective(): Vec3i = positioningEngine.getCenterVec3i()

	override fun onMove(movement: StarshipMovement) {
		pathfindingEngine.onMove(movement)
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
		val target = this.target ?: return returnToPreviousController()

		if (!ok) {
			aggressivenessLevel.disengage(this)
			return
		}

		positioningEngine.standoffDistance = getStandoffDistance(target)
		positioningEngine.tick()

		pathfindingEngine.destination = positioningEngine.findPositionVec3i()
		pathfindingEngine.tick()

		movementEngine.destination = pathfindingEngine.getNavPoint()
		movementEngine.tick()

		if (state == State.COMBAT) combatLoop()

		super.tick()
	}

	private fun combatLoop() {
		val target = this.target ?: return

		// Get the closest axis
		starship as ActiveControlledStarship
		starship.speedLimit = -1

		val faceDirection = vectorToBlockFace(getDirection(Vec3i(getCenter()), Vec3i(target.blocks.random())), includeVertical = false)

		fireAllWeapons(
			starship.centerOfMass,
			target.centerOfMass,
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
