package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.configuration.IntegerAmount
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

class BagSpawner(
	logger: Logger,
	locationProvider: Supplier<Location?>,
	private val budget: IntegerAmount,
	private val bagSpawnedShips: List<BagSpawnShip>,
	callback: (ActiveControlledStarship) -> Unit = {},
) : MultiSpawner(logger, locationProvider, callback) {
	override fun getShips(): List<GroupSpawnedShip> {
		var points = budget.get()
		val ships = mutableListOf<GroupSpawnedShip>()

		while (points > 0) {
			val remainingAvailable = bagSpawnedShips.filter { it.cost <= points }
			if (remainingAvailable.isEmpty()) break

			val ship = remainingAvailable.random()

			points -= ship.cost
			ships += ship.ship
		}

		return ships
	}

	data class BagSpawnShip(
		val ship: GroupSpawnedShip,
		val cost: Int,
	)
}
