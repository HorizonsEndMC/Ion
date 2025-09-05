package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * This spawner is not ticked normally, it is not registered.
 *
 * This spawner is constructed on the fly for each ship that would implement reinforcement mechanics.
 **/
class ReinforcementSpawner(
	private val reinforced: AIController,
	mechanic: SpawnerMechanic,
	val controllerModifiers: MutableList<Consumer<AIController>>
) : AISpawner(
	"NULL",
	mechanic
) {
	constructor(reinforced: AIController, reinforcementPool: List<AITemplate.SpawningInformationHolder>, controllerModifiers: MutableList<Consumer<AIController>>) : this(
		reinforced,
		SingleSpawn(
			WeightedShipSupplier(*reinforcementPool.toTypedArray()),
			formatLocationSupplier({ reinforced.getCenter().toLocation(reinforced.starship.world) }, 250.0, 500.0),
			null, // Calling module handles this
			{ _ -> Supplier { reinforced.getCoreModuleByType<DifficultyModule>()?.internalDifficulty ?: 2 } },
			{ reinforced.getCoreModuleByType<EnmityModule>()?.targetMode ?: AITarget.TargetMode.PLAYER_ONLY },
			{ reinforced.getUtilModule(AIFleetManageModule::class.java)?.fleet },
			{ controllerModifiers.forEach { it.accept(this) } }
		),
		controllerModifiers
	) {
		//check if reinforced ship is part of a fleet.
		var fleet = reinforced.getUtilModule(AIFleetManageModule::class.java)?.fleet
		if (fleet == null) {
			fleet = Fleets.createAIFleet()
			reinforced.addUtilModule(AIFleetManageModule(reinforced, fleet))
		}
	}

	override val scheduler: SpawnerScheduler = SpawnerScheduler.DummyScheduler(this)
}
