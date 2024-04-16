package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.positioning.PositioningModule
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class FollowModule(
	controller: AIController,
	val following: ActiveStarship?
) : PositioningModule(controller) {
	override fun findPosition(): Vec3i {
		TODO("Not yet implemented")
	}

	override fun getDestination(): Vec3i {
		TODO("Not yet implemented")
	}
}
