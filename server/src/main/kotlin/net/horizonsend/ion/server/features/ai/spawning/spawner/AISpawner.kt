package net.horizonsend.ion.server.features.ai.spawning.spawner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.ai.spawning.handleException
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.slf4j.Logger

abstract class AISpawner(
	val identifier: String,
	private val mechanic: SpawnerMechanic,
) {
	abstract val scheduler: SpawnerScheduler

	fun AIStarshipTemplate.getName(): Component = miniMessage().deserialize(miniMessageName)

	/** Entry point for the spawning mechanics, spawns the ship and handles any exceptions */
	fun trigger(logger: Logger, scope: CoroutineScope) = scope.launch {
		try { mechanic.trigger(logger) }
		catch (e: SpawningException) { handleException(logger, e) }
		catch (e: Throwable) {
			logger.error("An error occurred when attempting to execute spawner: ${e.message}")
			e.printStackTrace()
		}
	}

	fun getAvailableShips(): Collection<SpawnedShip> = mechanic.getAvailableShips()
}
