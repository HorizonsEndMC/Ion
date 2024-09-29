package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import org.slf4j.Logger

class MultiMechanic(vararg mechanics: SpawnerMechanic) : SpawnerMechanic() {
	val mechanics = mechanics.toList()

	override suspend fun trigger(logger: Logger) {
		mechanics.forEach { it.trigger(logger) }
	}
}
