package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience

abstract class Controller(val name: String, val starship: Starship) : ForwardingAudience.Single {
	override fun audience(): Audience = Audience.empty()
}
