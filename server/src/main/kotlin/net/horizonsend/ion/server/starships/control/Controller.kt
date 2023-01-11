package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.starlegacy.feature.starship.movement.StarshipMovement

interface Controller {
	val name: String
	val starship: Starship

	fun accelerationTick(): Triple<Int, Int, Int> = Triple(0, 0, 0)

	fun onShipMovement(starshipMovement: StarshipMovement) {}

	fun cleanup() {}
}
