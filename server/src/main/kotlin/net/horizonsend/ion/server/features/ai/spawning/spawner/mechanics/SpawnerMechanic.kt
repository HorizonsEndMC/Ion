package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import org.slf4j.Logger

abstract class SpawnerMechanic(protected val logger: Logger) {
	abstract suspend fun trigger()
}
