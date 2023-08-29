package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

/** Represents a previously piloted starship controller. **/
class UnpilotedController(player: Player, starship: Starship) : PlayerController(player, starship, "Unpiloted") {
	override val pilotName: Component = text("none")

	override val isShiftFlying: Boolean = false

	override val selectedDirectControlSpeed: Int = 0
}

