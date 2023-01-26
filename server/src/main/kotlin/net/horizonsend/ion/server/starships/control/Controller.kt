package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.starlegacy.feature.starship.movement.StarshipMovement

interface Controller {
	val name: String
	val starship: Starship

	fun tick() {}

	fun onShipMovement(starshipMovement: StarshipMovement) {}

	fun cleanup() {}
}
