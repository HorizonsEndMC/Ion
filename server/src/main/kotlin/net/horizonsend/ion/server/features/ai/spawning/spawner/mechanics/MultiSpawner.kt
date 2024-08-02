package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

abstract class MultiSpawner(private val locationProvider: Supplier<Location?>) : SpawnerMechanic() {
	abstract fun getShips(): List<SpawnedShip>

	override suspend fun trigger(logger: Logger) {
		val ships = getShips()
		val spawnOrigin = locationProvider.get() ?: return

		for (ship in ships) {
			val offset = ship.offset
			val absoluteHeight = ship.absoluteHeight
			val spawnPoint = if (offset != null) {
				if (absoluteHeight != null) {
					spawnOrigin.add(offset).apply { y = absoluteHeight }
				} else {
					spawnOrigin.add(offset)
				}
			} else spawnOrigin

			@Suppress("DeferredResultUnused")
			ship.spawn(logger, spawnPoint)
		}
	}

}
