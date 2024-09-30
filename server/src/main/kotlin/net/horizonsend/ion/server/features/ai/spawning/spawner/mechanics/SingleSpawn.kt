package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

class SingleSpawn(
	private val shipPool: ShipSupplier,
	private val locationProvider: Supplier<Location?>,
	/** 0: x, 1: y, 2: z, 3: world name, */
	private val spawnMessage: Component?,
	private val controllerModifier: AIController.() -> Unit = {}
) : SpawnerMechanic() {
	override suspend fun trigger(logger: Logger) {
		val template = shipPool.get()
		val spawnPoint = locationProvider.get() ?: return

		template.spawn(logger, spawnPoint, controllerModifier)

		if (spawnMessage != null) {
			IonServer.server.sendMessage(formatShipSpawnMessage(spawnMessage, template.template, spawnPoint.blockX, spawnPoint.blockY, spawnPoint.blockZ, spawnPoint.world.name))
		}
	}

	override fun getAvailableShips(): Collection<SpawnedShip> {
		return shipPool.getAllAvailable()
	}
}
