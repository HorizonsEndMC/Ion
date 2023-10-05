package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class NoOpMovementEngine(controller: AIController) : MovementEngine(controller) {
	override var destination: Vec3i = Vec3i(0, 0, 0)
	override fun tick() {}
}
