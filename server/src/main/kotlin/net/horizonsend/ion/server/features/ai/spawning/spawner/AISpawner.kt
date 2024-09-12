package net.horizonsend.ion.server.features.ai.spawning.spawner

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.ai.spawning.createAIShipFromTemplate
import net.horizonsend.ion.server.features.ai.spawning.handleException
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Location
import org.slf4j.Logger
import kotlin.random.Random

/**
 * This class is a definable AI spawner
 *
 * The spawner is executed via AISpawner#trigger
 *
 * The abstract method, AISpawner#triggerSpawn is used to control the behavior of the spawner.
 **/
interface AISpawner {
	/** The identifier of the spawner, used for configuration and locating the spawner. **/
	val identifier: String

	val pointChance: Double
	val pointThreshold: Int

	var points: Int
	var lastTriggered: Long

	/** Tick points, possibly trigger a spawn */
	fun tickPoints(logger: Logger) {
		handleSuccess(logger)

		if (Random.nextDouble() >= pointChance) return

		points++
	}

	private fun handleSuccess(logger: Logger) {
		if (points < pointThreshold) return

		points = 0

		lastTriggered = System.currentTimeMillis()
		trigger(logger, AISpawningManager.context)
	}

	fun AIStarshipTemplate.getName(): Component = miniMessage().deserialize(miniMessageName)

	/** Entry point for the spawning mechanics, spawns the ship and handles any exceptions */
	fun trigger(logger: Logger, scope: CoroutineScope) = scope.launch {
		try { triggerSpawn() }
		catch (e: SpawningException) { handleException(logger, e) }
		catch (e: Throwable) {
			logger.error("An error occurred when attempting to execute spawner: $identifier: ${e.message}")
			e.printStackTrace()
		}
	}

	/** The spawning logic, do as you wish */
	suspend fun triggerSpawn()

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
	) : Deferred<ActiveControlledStarship> {
		val deferred = CompletableDeferred<ActiveControlledStarship>()

		logger.info("Attempting to spawn AI starship ${template.identifier}")

		// Use the template to populate as much information as possible
		createAIShipFromTemplate(logger, template, location, controller) {
			deferred.complete(it)
			callback(it)
		}

		return deferred
	}
}
