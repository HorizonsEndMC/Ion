package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

class GroupSpawner(
	logger: Logger,
	locationProvider: Supplier<Location?>,
	callback: (ActiveControlledStarship) -> Unit,
	private val ships: MutableList<GroupSpawnedShip>
) : MultiSpawner(logger, locationProvider, callback) {
	override fun getShips(): List<GroupSpawnedShip> {
		return ships
	}
}
