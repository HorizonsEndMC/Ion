package net.horizonsend.ion.server.features.ai.spawning.spawner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.ai.spawning.createAIShipFromTemplate
import net.horizonsend.ion.server.features.ai.spawning.handleException
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Location
import org.slf4j.Logger

abstract class AISpawner(
	val identifier: String,
	private val mechanic: SpawnerMechanic,
) {
	abstract val scheduler: SpawnerScheduler

	fun AIStarshipTemplate.getName(): Component = miniMessage().deserialize(miniMessageName)

	/** Entry point for the spawning mechanics, spawns the ship and handles any exceptions */
	fun trigger(logger: Logger, scope: CoroutineScope) = scope.launch {
		try { mechanic.trigger(logger) }
		catch (e: SpawningException) { handleException(logger, e) }
		catch (e: Throwable) {
			logger.error("An error occurred when attempting to execute spawner: ${e.message}")
			e.printStackTrace()
		}
	}

	fun getAvailableShips(): Collection<SpawnedShip> = mechanic.getAvailableShips()
}

/**
 * Spawns the specified at the provided location
 *
 * @param template, The template for the starship it will attempt to place
 * @param location, The location where it will attempt to place the starship, may vary if obstructed
 * @param controller, The provided function to create the controller from the active starship
 *
 * The returned deferred is completed once the ship has been piloted.
 **/
fun spawnAIStarship(
	logger: Logger,
	template: AITemplate,
	location: Location,
	controller: (ActiveStarship) -> Controller,
	callback: (ActiveControlledStarship) -> Unit = {}
) {
	logger.info("Attempting to spawn AI starship ${template.identifier}")

	// Use the template to populate as much information as possible
	createAIShipFromTemplate(logger, template, location, controller) {
		callback(it)
	}
}

fun createController(template: AITemplate, pilotName: Component): (ActiveStarship) -> AIController {
	val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

	return { starship: ActiveStarship ->
		val controller = factory(starship, pilotName, template.starshipInfo.manualWeaponSets, template.starshipInfo.autoWeaponSets)
		controller.getModuleByType<ClosestTargetingModule>()?.maxRange = template.behaviorInformation.engagementRange

		template.behaviorInformation.additionalModules.forEach {
			controller.modules[it.name] = it.createModule(controller)
		}

		controller
	}
}
