package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.input.InputHandler
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException

abstract class MovementHandler(val controller: Controller, val name: String, open val input :InputHandler) {

	val starship = controller.starship
	open fun create() {}
	open fun destroy() {}

	open fun onBlocked(reason: StarshipMovementException) {}

	open fun tick() {}
}
