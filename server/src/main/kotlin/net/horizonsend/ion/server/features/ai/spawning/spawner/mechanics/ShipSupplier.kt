package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import java.util.function.Supplier

interface ShipSupplier : Supplier<AITemplate>

class SingleShipSupplier(private val template: AITemplate) : ShipSupplier {
	override fun get(): AITemplate {
		return template
	}
}

class WeightedShipSupplier(vararg templates: AITemplate.SpawningInformationHolder) : ShipSupplier {
	private val templates = templates.toList()

	override fun get(): AITemplate {
		return templates.weightedRandom { it.probability }.template
	}
}

class RandomShipSupplier(vararg val templates: AITemplate) : ShipSupplier {
	override fun get(): AITemplate {
		return templates.random()
	}
}
