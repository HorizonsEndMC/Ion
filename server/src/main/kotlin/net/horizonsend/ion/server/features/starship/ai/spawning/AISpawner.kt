package net.horizonsend.ion.server.features.starship.ai.spawning

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.module.misc.RadiusMessageModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.ReinforcementSpawnerModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp.ReinforcementSpawner
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.slf4j.Logger
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
	private val configurationSupplier: Supplier<AISpawningConfiguration.AISpawnerConfiguration>,
) {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)
	val configuration get() = configurationSupplier.get()

	private val pointChance get(): Double = configuration.pointChance
	private val pointThreshold get(): Int = configuration.pointThreshold

	private var points: Int = 0
	private var lastTriggered: Long = 0

	val minDistanceFromPlayer: Double get() = configuration.minDistanceFromPlayer
	val maxDistanceFromPlayer: Double get() = configuration.maxDistanceFromPlayer

	/** Tick points, possibly trigger a spawn */
	fun tickPoints() {
		handleSuccess()

		if (Random.nextDouble() >= pointChance) return

		points++
	}

	fun getPoints() = points

	fun setPoints(value: Int) { points = value }

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
	open fun createController(template: AIStarshipTemplate, pilotName: Component): (ActiveStarship) -> AIController {
		val factory = AIControllerFactories[template.controllerFactory]

		return { starship: ActiveStarship ->
			val controller = factory(starship, pilotName, template.manualWeaponSets, template.autoWeaponSets)

			controller.setColor(Color.fromRGB(template.color))
			controller.getModuleByType<ClosestTargetingModule>()?.maxRange = template.engagementRange

			template.smackInformation?.let { smackInformation ->
				val prefix = miniMessage().deserialize(smackInformation.prefix)

				if (smackInformation.messages.isEmpty()) return@let

				val messages = smackInformation.messages.map { it.miniMessage() }.toTypedArray()

				controller.modules["smack"] = SmackTalkModule(controller, prefix, *messages)
			}

			template.radiusMessageInformation?.let { messageInformation ->
				val prefix = miniMessage().deserialize(messageInformation.prefix)

				if (messageInformation.messages.isEmpty()) return@let

				val messages = messageInformation.messages.mapValues { it.value.miniMessage() }

				controller.modules["warning"] = RadiusMessageModule(controller, prefix, messages)
			}

			template.reinforcementInformation?.let {
				val spawner = ReinforcementSpawner(controller, it.configuration)

				val module = ReinforcementSpawnerModule(
					controller,
					spawner,
					it.activationThreshold,
					miniMessage().deserialize(it.broadcastMessage),
					delay = it.delay,
				)

				controller.modules["reinforcement"] = module
			}

			controller
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
