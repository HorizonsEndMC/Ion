package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import java.util.function.Supplier

interface ShipSupplier : Supplier<SpawnedShip>

class SingleShipSupplier(private val template: SpawnedShip) : ShipSupplier {
	override fun get(): SpawnedShip {
		return template
	}
}

class WeightedShipSupplier(vararg templates: AITemplate.SpawningInformationHolder) : ShipSupplier {
	private val templates = templates.toList()

	override fun get(): SpawnedShip {
		return templates.weightedRandom { it.probability }.template
	}
}

class RandomShipSupplier(vararg val templates: SpawnedShip) : ShipSupplier {
	override fun get(): SpawnedShip {
		return templates.random()
	}
}
