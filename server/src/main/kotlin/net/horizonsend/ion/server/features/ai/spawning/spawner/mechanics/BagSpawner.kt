package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.configuration.IntegerAmount
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import org.bukkit.Location
import java.util.function.Supplier

class BagSpawner(
	locationProvider: Supplier<Location?>,
	private val budget: IntegerAmount,
	vararg bagSpawnedShips: BagSpawnShip
) : MultiSpawner(locationProvider) {
	private val bagSpawnedShips: List<BagSpawnShip> = listOf(*bagSpawnedShips)

	override fun getShips(): List<SpawnedShip> {
		var points = budget.get()
		val ships = mutableListOf<SpawnedShip>()

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
		val ship: SpawnedShip,
		val cost: Int,
	)

	companion object {
		fun asBagSpawned(ship: SpawnedShip, cost: Int) = BagSpawnShip(ship, cost)
	}

	override fun getAvailableShips(): Collection<SpawnedShip> {
		return bagSpawnedShips.map { it.ship }
	}
}
