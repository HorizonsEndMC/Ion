package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import org.slf4j.Logger

class CallbackMechanic(val callback: () -> Unit) : SpawnerMechanic() {
	override suspend fun trigger(logger: Logger) {
		callback.invoke()
	}
}
