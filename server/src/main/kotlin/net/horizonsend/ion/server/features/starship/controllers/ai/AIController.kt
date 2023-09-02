package net.horizonsend.ion.server.features.starship.controllers.ai

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import java.util.UUID

abstract class AIController(starship: ActiveStarship, name: String, val uuid: UUID) : Controller(starship, name) {
	override var isShiftFlying: Boolean = false
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var selectedDirectControlSpeed: Int = 0

	var lastRotation: Long = 0L
}
