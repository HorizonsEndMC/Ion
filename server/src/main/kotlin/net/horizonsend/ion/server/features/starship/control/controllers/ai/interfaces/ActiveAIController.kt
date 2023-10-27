package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import co.aikar.commands.ConditionFailedException
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement

abstract class ActiveAIController(
	starship: ActiveStarship,
	name: String,
	damager: Damager,
	aggressivenessLevel: AggressivenessLevel
) : AIController(starship, name, damager, aggressivenessLevel) {
	abstract val positioningEngine: PositioningEngine
	abstract val pathfindingEngine: PathfindingEngine
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

	override fun onBlocked(movement: StarshipMovement, reason: ConditionFailedException) {
		positioningEngine.onBlocked(movement, reason)
		pathfindingEngine.onBlocked(movement, reason)
		movementEngine.onBlocked(movement, reason)
	}

	fun tickAll() {
		positioningEngine.tick()
		pathfindingEngine.tick()
		movementEngine.tick()
	}
}
