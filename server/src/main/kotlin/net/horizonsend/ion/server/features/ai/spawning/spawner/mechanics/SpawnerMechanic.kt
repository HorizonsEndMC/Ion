package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.kyori.adventure.text.Component
import org.slf4j.Logger

abstract class SpawnerMechanic {
	abstract suspend fun trigger(logger: Logger)

	abstract fun getAvailableShips(): Collection<SpawnedShip>

	// Utility
	/** 0: x, 1: y, 2: z, 3: world name, */
	protected fun formatShipSpawnMessage(message: Component, starship: AITemplate, x: Int, y: Int, z: Int, worldName: String): Component {
		return template(
			message = message,
			paramColor = HEColorScheme.HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			starship.starshipInfo.componentName(),
			x,
			y,
			z,
			worldName
		)
	}
}
