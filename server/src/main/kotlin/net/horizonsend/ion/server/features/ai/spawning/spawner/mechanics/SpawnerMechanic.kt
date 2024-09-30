package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.slf4j.Logger

abstract class SpawnerMechanic {
	abstract suspend fun trigger(logger: Logger)

	abstract fun getAvailableShips(): Collection<SpawnedShip>
}
