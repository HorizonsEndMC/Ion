package net.horizonsend.ion.server.features.starship.subsystem.misc.tractor

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.input.PlayerShiftFlightInput
import net.horizonsend.ion.server.features.starship.control.movement.ShiftFlightHandler

enum class TractorControlMode {
	FOLLOW(),
	WASD() {
		override fun onSetup(starship: Starship) {
			val controller = starship.controller
			if (controller !is ActivePlayerController) return

			controller.movementHandler = TractorWASDHandler(controller)
		}

		override fun onStop(starship: Starship) {
			val controller = starship.controller
			if (controller !is ActivePlayerController) return

			controller.movementHandler = ShiftFlightHandler(controller, PlayerShiftFlightInput(controller))
		}
	},
	LOOK() {
		override fun onSetup(starship: Starship) {
			val controller = starship.controller
			if (controller !is ActivePlayerController) return

			controller.movementHandler = TractorLookHandler(controller)
		}

		override fun onStop(starship: Starship) {
			val controller = starship.controller
			if (controller !is ActivePlayerController) return

			controller.movementHandler = ShiftFlightHandler(controller, PlayerShiftFlightInput(controller))
		}
	}

	;

	open fun onSetup(starship: Starship) {}
	open fun onStop(starship: Starship) {}
}
