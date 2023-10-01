package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.CombatController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.LocationObjectiveAI
import net.horizonsend.ion.server.features.starship.control.controllers.ai.util.NavigationEngine
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.randomDouble
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
class StarfighterCombatController(
	starship: ActiveStarship,
	override var target: ActiveStarship,
	private val previousController: Controller,
	aggressivenessLevel: AggressivenessLevel
) : AIController(starship, "combat", aggressivenessLevel), CombatController, LocationObjectiveAI {
	val navigationEngine: NavigationEngine = NavigationEngine(this, target.centerOfMass).apply {
		shouldRotateDuringShiftFlight = false
	}

	override fun destroy() {
		navigationEngine.shutDown()
		super.destroy()
	}

	override val pilotName: Component get() = text()
		.append(text("Small Craft Combat Intelligence", NamedTextColor.GRAY))
		.append(text(" "))
		.append(aggressivenessLevel.displayName)
		.build()

	override fun getTargetLocation(): Location = target.centerOfMass.toLocation(target.world)

	private val shields get() = starship.shields
	private val shieldCount get() = shields.size
	private val averageHealth get() = shields.sumOf { it.powerRatio } / shieldCount.toDouble()

	/** The location that should be navigated towards */
	private var locationObjective: Location = target.centerOfMass.toLocation(target.world)

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
	private fun getStandoffDistance() : Double {
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

	private fun disengage() {
		if (aggressivenessLevel.ordinal >= AggressivenessLevel.HIGH.ordinal) {
			val nextTarget = findNextTarget()

			if (nextTarget == null) fallback()

			else target = nextTarget
			return
		}

		fallback()
	}

	private fun findNextTarget(): ActiveStarship? {
		val nearbyShips = getNearbyShips(0.0, aggressivenessLevel.engagementDistance) { starship, _ ->
			starship.controller !is AIController
		}

		if (nearbyShips.isEmpty()) return null

		return nearbyShips.firstOrNull()
	}

	/** Returns to previous controller if there is no target left **/
	private fun fallback() {
		starship.controller = previousController
		return
	}

	/** Finds a location in the cardinal directions from the target at the engagement distance */
	private fun getClosestAxisPoint(): Vector {
		val shipLocation = getCenter().toVector()
		val targetLocation = target.centerOfMass.toVector()

		val cardinalOffsets = CARDINAL_BLOCK_FACES.map { it.direction.multiply(getStandoffDistance()) }
		val points = cardinalOffsets.map { targetLocation.clone().add(it) }

		return points.minBy { it.distance(shipLocation) }
	}

	override fun getObjective(): Vec3i = Vec3i(getClosestAxisPoint())

	override fun onMove(movement: StarshipMovement) {
		navigationEngine.onMove(movement)
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

		if (!ok) {
			disengage()
			return
		}

		navigationEngine.tick()
		if (state == State.COMBAT) combatLoop()

		super.tick()
	}

	private fun combatLoop() {
		// Get the closest axis
		starship as ActiveControlledStarship
		starship.speedLimit = -1

		val faceDirection = getDirection(Vec3i(getCenter()), Vec3i(target.blocks.random()))
		var direction = getDirection(Vec3i(getCenter()), target.centerOfMass).normalize()

		if (aggressivenessLevel.shotDeviation > 0) {
			val offsetX = randomDouble(-aggressivenessLevel.shotDeviation, aggressivenessLevel.shotDeviation)
			val offsetY = randomDouble(-aggressivenessLevel.shotDeviation, aggressivenessLevel.shotDeviation)
			val offsetZ = randomDouble(-aggressivenessLevel.shotDeviation, aggressivenessLevel.shotDeviation)

			direction = direction.clone().add(Vector(offsetX, offsetY, offsetZ)).normalize()
		}

		Tasks.sync {
			AIControlUtils.faceDirection(this, vectorToBlockFace(faceDirection, false))
			AIControlUtils.shootInDirection(this, direction, leftClick = false, target = getTargetLocation().toVector())
			AIControlUtils.shootInDirection(this, direction, leftClick = true, target = getTargetLocation().toVector())
		}
	}
}
