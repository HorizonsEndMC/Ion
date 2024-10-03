package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.function.Supplier

/**
 * Will always spawn the list of provided ships
 **/
class GroupSpawner(
	locationProvider: Supplier<Location?>,
	private val ships: MutableList<SpawnedShip>,
	groupMessage: Component?,
	individualSpawnMessage: SpawnMessage?,
) : MultiSpawner(locationProvider, groupMessage, individualSpawnMessage) {
	override fun getShips(): List<SpawnedShip> {
		return ships
	}

	override fun getAvailableShips(): Collection<SpawnedShip> {
		return ships
	}
}
