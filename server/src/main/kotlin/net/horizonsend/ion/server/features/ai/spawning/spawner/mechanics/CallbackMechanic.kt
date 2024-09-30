package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.slf4j.Logger

class CallbackMechanic(val callback: () -> Unit) : SpawnerMechanic() {
	override suspend fun trigger(logger: Logger) {
		callback.invoke()
	}

	override fun getAvailableShips(): Collection<SpawnedShip> {
		return listOf()
	}
}
