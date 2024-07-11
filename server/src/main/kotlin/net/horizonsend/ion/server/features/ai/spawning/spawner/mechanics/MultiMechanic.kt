package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import org.slf4j.Logger

class MultiMechanic(logger: Logger, vararg mechanics: SpawnerMechanic) : SpawnerMechanic(logger) {
	val mechanics = mechanics.toList()

	override suspend fun trigger() {
		mechanics.forEach { it.trigger() }
	}
}
