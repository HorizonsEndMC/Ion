package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience

abstract class Controller(val starship: Starship, val name: String) : ForwardingAudience.Single {
	override fun audience(): Audience = Audience.empty()

	/** Called when a controller is removed by a starship, any logic to restore player state should be done here. */
	open fun destroy() {}
}
