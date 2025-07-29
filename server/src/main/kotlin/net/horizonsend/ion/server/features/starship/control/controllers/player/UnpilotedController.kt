package net.horizonsend.ion.server.features.starship.control.controllers.player

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.input.NoInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

class UnpilotedController(oldPilot: Player, starship: ActiveStarship) : PlayerController(oldPilot, starship, "Unpiloted") {
	override var movementHandler: MovementHandler =
		object : MovementHandler(this,"Unpiloted", NoInput(this)) {}


	// Shouldn't be treated like they're still piloting it
	override fun audience(): Audience = Audience.empty()

	override val pilotName: Component = text("none")

	constructor(controller: PlayerController) : this(controller.player, controller.starship)
}
