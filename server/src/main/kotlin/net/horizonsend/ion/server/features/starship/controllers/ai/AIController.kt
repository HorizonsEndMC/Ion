package net.horizonsend.ion.server.features.starship.controllers.ai

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.controllers.Controller

abstract class AIController(starship: Starship, name: String) : Controller(starship, name) {
	override var isShiftFlying: Boolean = false
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var selectedDirectControlSpeed: Int = 0
}
