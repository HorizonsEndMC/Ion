package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.SpawnerScheduler
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

/**
 * This spawner is not ticked normally, it is not registered.
 *
 * This spawner is constructed on the fly for each ship that would implement reinforcement mechanics.
 **/
class ReinforcementSpawner(
	private val reinforced: AIController,
	mechanic: SpawnerMechanic
) : AISpawner(
	"NULL",
	mechanic
) {
	constructor(reinforced: AIController, reinforcementPool: List<AITemplate.SpawningInformationHolder>) : this(
		reinforced,
		SingleSpawn(
			WeightedShipSupplier(*reinforcementPool.toTypedArray()),
			formatLocationSupplier({ reinforced.getCenter().toLocation(reinforced.starship.world) }, 250.0, 500.0),
			null, // Calling module handles this
			::setupReinforcementShip
		)
	)

	override val scheduler: SpawnerScheduler = SpawnerScheduler.DummyScheduler(this)

	companion object {
		fun setupReinforcementShip(controller: AIController) {/*TODO*/}
	}
}
