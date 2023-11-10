package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.AStarPathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.MovementException
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component

abstract class ActiveAIController(
	starship: ActiveStarship,
	name: String,
	damager: Damager,
	pilotName: Component?,
	aggressivenessLevel: AggressivenessLevel,
) : AIController(starship, name, damager, pilotName, aggressivenessLevel) {
	abstract val positioningEngine: PositioningEngine
	abstract val pathfindingEngine: AStarPathfindingEngine
	abstract val movementEngine: MovementEngine

	fun shutDownAll() {
		positioningEngine.shutDown()
		pathfindingEngine.shutDown()
		movementEngine.shutDown()
	}

	override fun onDamaged(damager: Damager) {
		positioningEngine.onDamaged(damager)
		pathfindingEngine.onDamaged(damager)
		movementEngine.onDamaged(damager)

		aggressivenessLevel.onDamaged(this, damager)
	}

	override fun onMove(movement: StarshipMovement) {
		positioningEngine.onMove(movement)
		pathfindingEngine.onMove(movement)
		movementEngine.onMove(movement)
	}

	override fun onBlocked(movement: StarshipMovement, reason: MovementException, location: Vec3i?) {
		positioningEngine.onBlocked(movement, reason, location)
		pathfindingEngine.onBlocked(movement, reason, location)
		movementEngine.onBlocked(movement, reason, location)
	}

	fun tickAll() {
		positioningEngine.tick()
		pathfindingEngine.tick()
		movementEngine.tick()
	}
}
