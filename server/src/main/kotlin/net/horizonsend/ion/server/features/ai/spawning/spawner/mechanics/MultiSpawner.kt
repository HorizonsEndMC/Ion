package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.spawner.spawnAIStarship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

abstract class MultiSpawner(
	logger: Logger,
	private val locationProvider: Supplier<Location?>,
	private val callback: (ActiveControlledStarship) -> Unit = {}
) : SpawnerMechanic(logger) {
	abstract fun getShips(): List<GroupSpawnedShip>

	override suspend fun trigger() {
		val ships = getShips()
		val spawnPoint = locationProvider.get() ?: return

		for (ship in ships) {
			ship.spawn(logger, spawnPoint, callback)
		}
	}

	data class GroupSpawnedShip(
		val template: AITemplate,
		val nameProvider: Supplier<Component>,
		val controllerModifier: AIController.() -> Unit = {},
	) {
		fun spawn(logger: Logger, location: Location, callback: (ActiveControlledStarship) -> Unit) {
			val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

			@Suppress("DeferredResultUnused")
			spawnAIStarship(
        		logger,
        		template,
				location,
				{
        		    val controller = factory.invoke(it, nameProvider.get())
        		    controllerModifier.invoke(controller)

        		    controller
        		},
        		callback
    		)
		}
	}
}
