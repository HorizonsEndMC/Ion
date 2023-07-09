package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.entity.Player

abstract class Controller(val starship: Starship, val name: String) : ForwardingAudience.Single {
	override fun audience(): Audience = Audience.empty()

	/** Called on each server tick. */
	open fun tick() {}

	/** Called when the controller or its ship is removed. Any cleanup logic should be done here. */
	open fun destroy() {}

	val playerPilot: Player? get() = (this as? PlayerController)?.player
}
