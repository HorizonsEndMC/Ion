package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.input.DirectCruiseControlInput
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace

class DirectCruiseControlHandler(controller: Controller, override val input: DirectCruiseControlInput
) : MovementHandler(controller, "Cruise Control",	input) {
	val player get() = (controller as? PlayerController)?.player

	override fun create() {
		input.create()
	}

	override fun destroy() {
		starship.cruiseData.targetSpeed = 0
		input.destroy()
	}

	override fun tick() {
		if (starship.isTeleporting) return

		if (starship.type == StarshipType.PLATFORM) return controller.userErrorAction("This ship type is not capable of moving.")

		if (Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) {
			starship.setDirectCruiseControlEnabled(false)
			return
		}

		input.getData()
	}
}
