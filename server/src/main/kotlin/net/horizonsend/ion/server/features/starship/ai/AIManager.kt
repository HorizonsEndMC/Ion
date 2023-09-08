package net.horizonsend.ion.server.features.starship.ai

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

object AIManager : IonServerComponent() {
	val activeShips = mutableListOf<ActiveStarship>()

	override fun onDisable() {
		for (activeShip in activeShips) {
			StarshipDestruction.vanish(activeShip)
		}
	}

	fun all() = activeShips

	fun findByController(controller: AIController) = activeShips.firstOrNull { it.controller === controller }
}
