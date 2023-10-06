package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class ShiftFlightMovementEngine(
	controller: AIController,
	override var destination: Vec3i?
) : MovementEngine(controller) {
	override var starshipLocation: Vec3i = controller.starship.centerOfMass
		get() = controller.getCenterVec3i()


}
