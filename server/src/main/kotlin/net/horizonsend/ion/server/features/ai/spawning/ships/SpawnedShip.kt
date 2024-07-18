package net.horizonsend.ion.server.features.ai.spawning.ships

import kotlinx.coroutines.Deferred
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.spawner.spawnAIStarship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger

interface SpawnedShip {
	val template: AITemplate
	fun createController(logger: Logger, starship: ActiveStarship): AIController
	fun getName(logger: Logger): Component

	fun spawn(
		logger: Logger,
		location: Location
	): Deferred<ActiveControlledStarship> {
		return spawnAIStarship(
			logger,
			template,
			location,
			{ createController(logger, it) }
		)
	}
}
