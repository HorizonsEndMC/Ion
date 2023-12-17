package net.horizonsend.ion.server.features.starship.ai.module.positioning

import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

abstract class PositioningModule(controller: AIController) : AIModule(controller) {
	abstract fun findPosition(): Location
	abstract fun findPositionVec3i(): Vec3i
	abstract fun getDestination(): Vec3i
}
