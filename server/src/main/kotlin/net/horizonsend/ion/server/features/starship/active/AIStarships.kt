package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.controllers.ai.AIController

object AIStarships : IonServerComponent() {
	val aiStarships = mutableMapOf<AIController, ActiveControlledStarship>()

	fun create(starship: ActiveControlledStarship) {

	}

	// TODO AI types, registry, ai functions
}
