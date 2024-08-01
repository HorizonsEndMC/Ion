package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
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
			val spawnPoint = if (offset != null) {
				spawnOrigin.getLocationNear(
					offset.minDistanceFromCenter,
					offset.maxDistanceFromCenter)
					.apply { y = offset.yLevel }
			} else spawnOrigin

			@Suppress("DeferredResultUnused")
			ship.spawn(logger, spawnPoint)
		}
	}

}
