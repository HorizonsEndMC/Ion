package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

abstract class PositioningEngine(controller: AIController) : AIEngine(controller) {
	abstract fun findPosition(): Location
	abstract fun findPositionVec3i(): Vec3i

	abstract fun getDestination(): Vec3i
}
