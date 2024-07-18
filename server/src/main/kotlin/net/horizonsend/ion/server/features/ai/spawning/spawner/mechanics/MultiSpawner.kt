package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

abstract class MultiSpawner(private val locationProvider: Supplier<Location?>) : SpawnerMechanic() {
	abstract fun getShips(): List<SpawnedShip>

	override suspend fun trigger(logger: Logger) {
		val ships = getShips()
		val spawnPoint = locationProvider.get() ?: return

		for (ship in ships) {
			@Suppress("DeferredResultUnused")
			ship.spawn(logger, spawnPoint)
		}
	}

}
