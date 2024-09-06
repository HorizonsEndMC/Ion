package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement

class ShipTransportManager(
	val starship: Starship
) {
	init {
	    load()
	}

	fun load() {

	}

	fun displace(movement: StarshipMovement) {

	}
}
