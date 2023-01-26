package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.starlegacy.feature.starship.movement.StarshipMovement

interface Controller : ForwardingAudience.Single {
	val name: String
	val starship: Starship

	fun tick() {}

	fun onShipMovement(starshipMovement: StarshipMovement) {}

	fun cleanup() {}

	override fun audience(): Audience = Audience.empty()
}
