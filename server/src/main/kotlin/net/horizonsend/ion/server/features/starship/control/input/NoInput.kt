package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.server.features.starship.control.controllers.Controller

class NoInput(override val controller: Controller) : InputHandler {
	override fun getData(): Any {
		return false
	}
}
