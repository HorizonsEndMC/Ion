package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

class SingleSpawn(
	private val shipPool: ShipSupplier,
	private val locationProvider: Supplier<Location?>,
	/** 0: x, 1: y, 2: z, 3: world name, */
	private val spawnMessage: SpawnMessage?,
	private val controllerModifier: AIController.() -> Unit = {}
) : SpawnerMechanic() {
	override suspend fun trigger(logger: Logger) {
		val ship = shipPool.get()
		val spawnPoint = locationProvider.get() ?: return

		ship.spawn(logger, spawnPoint, controllerModifier)

		spawnMessage?.broadcast(spawnPoint, ship.template)
	}

	override fun getAvailableShips(): Collection<SpawnedShip> {
		return shipPool.getAllAvailable()
	}
}
