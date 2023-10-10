package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement

interface ActiveAIController {
	val positioningEngine: PositioningEngine
	val pathfindingEngine: PathfindingEngine
	val movementEngine: MovementEngine

	fun shutDownAll() {
		positioningEngine.shutDown()
		pathfindingEngine.shutDown()
		movementEngine.shutDown()
	}

	fun passDamage(damager: Damager) {
		positioningEngine.onDamaged(damager)
		pathfindingEngine.onDamaged(damager)
		movementEngine.onDamaged(damager)
	}

	fun passMovement(movement: StarshipMovement) {
		positioningEngine.onMove(movement)
		pathfindingEngine.onMove(movement)
		movementEngine.onMove(movement)
	}

	fun tickAll() {
		positioningEngine.tick()

		pathfindingEngine.destination = positioningEngine.findPositionVec3i()
		pathfindingEngine.tick()

		movementEngine.destination = pathfindingEngine.getNavPoint()
		movementEngine.tick()
	}
}
