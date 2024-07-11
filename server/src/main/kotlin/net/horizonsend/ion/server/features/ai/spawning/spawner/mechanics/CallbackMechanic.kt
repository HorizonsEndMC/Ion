package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import org.slf4j.Logger

class CallbackMechanic(logger: Logger, val callback: () -> Unit) : SpawnerMechanic(logger) {
	override suspend fun trigger() {
		callback.invoke()
	}
}
