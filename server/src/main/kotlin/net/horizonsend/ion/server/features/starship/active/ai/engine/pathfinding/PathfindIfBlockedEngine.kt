package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class PathfindIfBlockedEngine(
	controller: AIController,
	destination: Vec3i?
) : PathfindingEngine(controller, destination) {
	override fun tick() {
		super.tick()
	}
}
