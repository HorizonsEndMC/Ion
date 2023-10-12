package net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.CruiseEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindIfBlockedEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.BasicPositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.NeutralAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
	val combatController: (AIController, ActiveStarship) -> AIController
) : AIController(starship, "autoCruise", aggressivenessLevel),
	NeutralAIController,
	ActiveAIController {
	override var pathfindingEngine = PathfindIfBlockedEngine(this, Vec3i(destination))
	override var movementEngine = CruiseEngine(this, Vec3i(destination))
	override var positioningEngine = BasicPositioningEngine(this, destination)

	var ticks = 0

	override val pilotName: Component = starship.getDisplayNameComponent().append(Component.text(" [NEUTRAL]", NamedTextColor.YELLOW))

	val direction: Vector get() = destination.toVector().subtract(getCenter().toVector())

	/** Checks for nearby aggressive ships to enter a combat mode */
	private fun searchNearbyShips() {
		val nearbyShip = getNearbyShips(0.0, aggressivenessLevel.engagementDistance) { starship, _ ->
			starship.controller !is AIController
		}.firstOrNull()

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

	override fun createCombatController(controller: AIController, target: ActiveStarship): AIController {
		return combatController(controller, target)
	}

	override fun onMove(movement: StarshipMovement) {
		passMovement(movement)
	}

	override fun onDamaged(damager: Damager) {
		passDamage(damager)
		if (damager is Controller) return combatMode(this, damager.starship)
	}
}
