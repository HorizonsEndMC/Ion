package net.horizonsend.ion.server.features.starship.controllers.ai

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.keysSortedByValue
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location

object AIControllers {
	fun dumbAI(starship: ActiveStarship): AIController = createController(
		starship,
		"dumbAI",
		displayName = text("eeeevil solesey ship", NamedTextColor.DARK_RED),
		Criterias.followAndShoot
	)

	private fun createController(
		starship: ActiveStarship,
		name: String,
		displayName: Component = text("name"),
		vararg critera: Criterias.Criteria
	): AIController {
		return object : AIController(starship, name, critera) {
			override val pilotName: Component = displayName
		}
	}

	fun getNearestPlayer(controller: Controller, location: Location) = IonServer.server.onlinePlayers
		.filter { it.world == controller.starship.world }
		.associateWith { it.location.distance(location) }
		.filter { it.value <= 5000 }
		.filter { it.value >= 50 }
		.keysSortedByValue()
		.firstOrNull()
}
