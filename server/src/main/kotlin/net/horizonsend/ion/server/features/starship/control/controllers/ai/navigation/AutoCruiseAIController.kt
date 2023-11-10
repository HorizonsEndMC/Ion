package net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.CruiseEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindIfBlockedEngineAStar
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.BasicPositioningEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.NeutralAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

/**
 * General use Auto cruise AI controller
 *
 * The method to create the combat controller upon becoming aggressive can be passed in as a lambda,
 * allowing this to be applied to many different types of ship.
 **/
class AutoCruiseAIController(
	starship: ActiveStarship,
	var destination: Location,
	var maxSpeed: Int = -1,
	aggressivenessLevel: AggressivenessLevel,
	pilotName: Component?,
	val combatController: (AIController, AITarget) -> AIController
) : ActiveAIController(starship, "autoCruise", AIShipDamager(starship), pilotName, aggressivenessLevel),
	NeutralAIController {
	override var positioningEngine = BasicPositioningEngine(this, destination)
	override var pathfindingEngine = PathfindIfBlockedEngineAStar(this, positioningEngine)
	override var movementEngine = CruiseEngine(this, pathfindingEngine, Vec3i(destination), CruiseEngine.ShiftFlightType.IF_BLOCKED_AND_MATCH_Y)

	var ticks = 0

	val direction: Vector get() = destination.toVector().subtract(getCenter().toVector())

	/** Checks for nearby aggressive ships to enter a combat mode */
	private fun searchNearbyShips() = Tasks.sync {
		val nearbyShip = aggressivenessLevel.getNearbyTargets(this)

		// Switch to combat controller if nearby ship meets criteria
		nearbyShip?.let { starship.controller = combatController(this, it) }
	}

	/**
	 * Checks if this starship should be removed
	 *
	 * Conditions:
	 *  - Outside worldborder
	 *  - Reached objective
	 *  - Too old
	 *  - Destination Reached
	 *
	 *  Returns true if it should be removed
	 **/
	private fun checkRemoval(): Boolean {
		// If it was damaged in the last 5 minutes don't do anything
		starship.lastDamagedOrNull()?.let {
			if (it <= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)) return true
		}

		val origin = getCenter()

		// If it's an hour old
		if (ticks > TimeUnit.HOURS.toMillis(1)) return true

		// if within 100 blocks of destination
		if (distanceSquared(origin.toVector(), destination.toVector()) <= 10000) return true

		return !starship.world.worldBorder.isInside(origin)
	}

	override fun tick() = Tasks.async {
		ticks++

		// Only tick every quarter second
		if ((ticks % 5) != 0) return@async

		searchNearbyShips()

		if (checkRemoval()) {
			scheduleDespawn()
			return@async
		}

		movementEngine.speedLimit = maxSpeed
		tickAll()

		super.tick()
	}

	override fun createCombatController(controller: AIController, target: AITarget): AIController {
		return combatController(controller, target)
	}
}
