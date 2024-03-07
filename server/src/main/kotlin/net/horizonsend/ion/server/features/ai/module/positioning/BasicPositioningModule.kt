package net.horizonsend.ion.server.features.ai.module.positioning

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

class BasicPositioningModule(controller: AIController, private val goal: Vec3i) : PositioningModule(controller) {
	override fun findPosition(): Vec3i = goal

	override fun getDestination(): Vec3i = Vec3i(findPosition())
}
