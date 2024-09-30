package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import java.util.function.Supplier

interface ShipSupplier : Supplier<SpawnedShip> {
	fun getAllAvailable(): Collection<SpawnedShip>
}

class SingleShipSupplier(private val template: SpawnedShip) : ShipSupplier {
	override fun get(): SpawnedShip {
		return template
	}

	override fun getAllAvailable(): Collection<SpawnedShip> {
		return listOf(template)
	}
}

class WeightedShipSupplier(vararg templates: AITemplate.SpawningInformationHolder) : ShipSupplier {
	private val templates = templates.toList()

	override fun get(): SpawnedShip {
		return templates.weightedRandom { it.probability }.template
	}

	override fun getAllAvailable(): Collection<SpawnedShip> {
		return templates.map { it.template }
	}
}

class RandomShipSupplier(vararg val templates: SpawnedShip) : ShipSupplier {
	override fun get(): SpawnedShip {
		return templates.random()
	}

	override fun getAllAvailable(): Collection<SpawnedShip> {
		return templates.toList()
	}
}
