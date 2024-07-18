package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.bukkit.Location
import java.util.function.Supplier

/**
 * Will always spawn the list of provided ships
 **/
class GroupSpawner(
	locationProvider: Supplier<Location?>,
	private val ships: MutableList<SpawnedShip>
) : MultiSpawner(locationProvider) {
	override fun getShips(): List<SpawnedShip> {
		return ships
	}
}
