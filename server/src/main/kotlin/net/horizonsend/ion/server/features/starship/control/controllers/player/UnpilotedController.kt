package net.horizonsend.ion.server.features.starship.control.controllers.player

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

class UnpilotedController(oldPilot: Player, starship: ActiveStarship) : PlayerController(oldPilot, starship, "Unpiloted") {
	override val selectedDirectControlSpeed: Int = 0
	override val yaw: Float = 0f
	override val pitch: Float = 0f

	override fun isSneakFlying(): Boolean = false

	// Shouldn't be treated like they're still piloting it
	override fun audience(): Audience = Audience.empty()

	override fun getPilotName(): Component = text("none")

	constructor(controller: PlayerController) : this(controller.player, controller.starship)
}
