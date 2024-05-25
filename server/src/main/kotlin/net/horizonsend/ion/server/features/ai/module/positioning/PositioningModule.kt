package net.horizonsend.ion.server.features.ai.module.positioning

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

abstract class PositioningModule(controller: AIController) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	abstract fun findPosition(): Vec3i?
	abstract fun getDestination(): Vec3i?
}
