package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

class SingleSpawn(
	private val shipPool: ShipSupplier,
	private val locationProvider: Supplier<Location?>
) : SpawnerMechanic() {
	override suspend fun trigger(logger: Logger) {
		val template = shipPool.get()
		val spawnPoint = locationProvider.get() ?: return

		template.spawn(logger, spawnPoint)
	}

	override fun getAvailableShips(): Collection<SpawnedShip> {
		return shipPool.getAllAvailable()
	}
}
