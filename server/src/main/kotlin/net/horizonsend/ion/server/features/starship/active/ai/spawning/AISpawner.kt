package net.horizonsend.ion.server.features.starship.active.ai.spawning

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawningUtils.createAIShipFromTemplate
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement.placeImmediate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
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
	private val pointChance: Double,
	private val pointThreshold: Int
) {
	val configuration get() = configurationSupplier.get()
	protected val log = LoggerFactory.getLogger(javaClass)

	private var points: Int = 0
	private var lastTriggered: Long = 0

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
	fun trigger(context: CoroutineScope) = context.launch {
		try { triggerSpawn() }
		catch (e: SpawningException) { handleException(e) }
		catch (e: Throwable) {
			log.error("An error occurred when attempting to execute spawner: $identifier: ${e.message}")
			e.printStackTrace()
		}
	}

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

		// Use the template to populate as much information as possible
		createAIShipFromTemplate(template, location, controller) {
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

		return { starship ->
			factory.createController(
				starship,
				pilotName,
				null,
				null,
				template.manualWeaponSets,
				template.autoWeaponSets,
				null // No previous
			)
		}
	}

	/** Handle any exceptions with spawning */
	private fun handleException(exception: SpawningException) {
		log.warn(exception.message)

		val blockKeys = exception.blockLocations

		// Delete a ship that did not detect properly
		if (blockKeys.isNotEmpty()) {
			val airQueue = Long2ObjectOpenHashMap<BlockState>(blockKeys.size)
			val air = Blocks.AIR.defaultBlockState()

			blockKeys.associateWithTo(airQueue) { air }

			placeImmediate(exception.world, airQueue)
		}
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
}
