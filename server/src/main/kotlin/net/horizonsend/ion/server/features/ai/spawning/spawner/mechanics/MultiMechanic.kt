package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.slf4j.Logger

class MultiMechanic(vararg mechanics: SpawnerMechanic) : SpawnerMechanic() {
	val mechanics = mechanics.toList()

	override fun trigger(logger: Logger) {
		mechanics.forEach { it.trigger(logger) }
	}

	override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> {
		return mechanics.flatMap { it.getAvailableShips() }
	}
}
