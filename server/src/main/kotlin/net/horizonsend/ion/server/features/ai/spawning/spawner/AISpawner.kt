package net.horizonsend.ion.server.features.ai.spawning.spawner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.ai.spawning.handleException
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler
import org.slf4j.Logger

abstract class AISpawner(
	val identifier: String,
	private val mechanic: SpawnerMechanic,
) {
	abstract val scheduler: SpawnerScheduler

	/** Entry point for the spawning mechanics, spawns the ship and handles any exceptions */
	open fun trigger(logger: Logger, scope: CoroutineScope) = scope.launch {
		logger.info("AI Spawner $identifier triggered.")
		try {
			mechanic.trigger(logger)
		} catch (e: SpawningException) {
			handleException(logger, e)
		} catch (e: Throwable) {
			logger.error("An error occurred when attempting to execute spawner: ${e.message}")
			e.printStackTrace()
		}
	}

	fun getAvailableShips(): Collection<SpawnedShip> = mechanic.getAvailableShips()
}
