package net.horizonsend.ion.server.features.starship.ai.spawning

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Location
import org.bukkit.World
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import kotlin.random.Random

/**
 * This class is a definable AI spawner
 *
 * The spawner is executed via AISpawner#trigger
 *
 * The abstract method, AISpawner#triggerSpawn is used to control the behavior of the spawner.
 *
 * @param identifier The identifier of the spawner, used for configuration and locating the spawner.
 * @param configurationSupplier The defined ships for this spawner.
 **/
abstract class AISpawner(
	val identifier: String,
	private val configurationSupplier: Supplier<AIShipConfiguration.AISpawnerConfiguration>,
) {
	private val pointChance: Double = configuration.pointChance
	private val pointThreshold: Int = configuration.pointThreshold

	val configuration get() = configurationSupplier.get()
	protected val log = LoggerFactory.getLogger(javaClass)

	private var points: Int = 0
	private var lastTriggered: Long = 0

	val minDistanceFromPlayer: Double get() = configuration.minDistanceFromPlayer
	val maxDistanceFromPlayer: Double get() = configuration.maxDistanceFromPlayer

	/** Tick points, possibly trigger a spawn */
	fun tickPoints() {
		handleSuccess()

		if (Random.nextDouble() < pointChance) return

		points++
	}

	private fun handleSuccess() {
		if (points < pointThreshold) return

		points = 0

		lastTriggered = System.currentTimeMillis()
		trigger(AISpawningManager.context)
	}

	fun AIStarshipTemplate.getName(): Component = miniMessage().deserialize(miniMessageName)

	/** Entry point for the spawning mechanics, spawns the ship and handles any exceptions */
	fun trigger(scope: CoroutineScope) = scope.launch {
		try { triggerSpawn() }
		catch (e: SpawningException) { handleException(log, e) }
		catch (e: Throwable) {
			log.error("An error occurred when attempting to execute spawner: $identifier: ${e.message}")
			e.printStackTrace()
		}
	}

	/** Checks if the position of the spawn is valid */
	protected abstract fun spawningConditionsMet(world: World, x: Int, y: Int, z: Int): Boolean

	/** The spawning logic, do as you wish */
	protected abstract suspend fun triggerSpawn()

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
		template: AIStarshipTemplate,
		location: Location,
		controller: (ActiveStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) : Deferred<ActiveControlledStarship> {
		val deferred = CompletableDeferred<ActiveControlledStarship>()

		log.info("Attempting to spawn AI starship ${template.identifier}")

		// Use the template to populate as much information as possible
		createAIShipFromTemplate(log, template, location, controller) {
			deferred.complete(it)
			callback(it)
		}

		return deferred
	}

	/**
	 * This method creates the controller for the spawned ship. It can be used to define the behavior of the vessel.
	 *
	 * @return A function used to create the controller for the starship
	 **/
	open fun createController(template: AIStarshipTemplate, pilotName: Component): (ActiveStarship) -> Controller {
		val factory = AIControllerFactories[template.controllerFactory]

		return { starship -> factory(starship, pilotName, template.manualWeaponSets, template.autoWeaponSets) }
	}

	/** An exception relating to a cause of a failed spawn. */
	class SpawningException(
		message: String,
		val world: World,
		val spawningLocation: Vec3i?,
	): Throwable(message) {
		/** The locations of any placed blocks. Will be empty if the error occured before any were placed. */
		var blockLocations: LongOpenHashSet = LongOpenHashSet()
	}

	/** Selects a starship template off of the configuration, picks, and serializes a name */
	open fun getStarshipTemplates(world: World): Collection<Pair<AIStarshipTemplate, Component>> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
		val worldConfig = configuration.getWorld(world)!!
		val tierIdentifier = worldConfig.tierWeightedRandomList.random()
		val tier = configuration.getTier(tierIdentifier)
		val shipIdentifier = tier.shipsWeightedList.random()
		val name = miniMessage().deserialize(tier.namesWeightedList.random())

		return listOf(IonServer.aiShipConfiguration.getShipTemplate(shipIdentifier) to name)
	}
}
