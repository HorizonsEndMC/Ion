package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.ai.module.positioning.PositioningModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

class FollowModule(
	controller: AIController,
	val following: ActiveStarship?
) : PositioningModule(controller) {
	override fun findPosition(): Location {
		TODO("Not yet implemented")
	}

	override fun findPositionVec3i(): Vec3i {
		TODO("Not yet implemented")
	}

	override fun getDestination(): Vec3i {
		TODO("Not yet implemented")
	}

}
