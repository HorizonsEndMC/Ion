package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import org.slf4j.Logger

abstract class SpawnerMechanic {
	abstract suspend fun trigger(logger: Logger)
}
