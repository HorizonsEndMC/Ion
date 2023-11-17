package net.horizonsend.ion.server.features.starship.active.ai.spawning

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.AIManager
import net.horizonsend.ion.server.features.starship.active.ai.AIStarshipFactory.createAIShipFromTemplate
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random

abstract class AISpawner(val identifier: String) {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	abstract val config: AIShipConfiguration.AISpawnerConfiguration

	abstract fun findLocation(): Location?

	open fun getTemplate(world: World): Pair<AIStarshipTemplate, Component> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
		val worldConfig = config.getWorld(world)!!
		val tierIdentifier = worldConfig.tierWeightedRandomList.random()
		val tier = config.getTier(tierIdentifier)
		val shipIdentifier = tier.shipsWeightedList.random()
		val name = MiniMessage.miniMessage().deserialize(tier.namesWeightedList.random())

		return IonServer.aiShipConfiguration.getShipTemplate(shipIdentifier) to name
	}

	abstract fun spawn(location: Location, callback: (ActiveControlledStarship) -> Unit = {}): Deferred<ActiveControlledStarship>

	@OptIn(ExperimentalCoroutinesApi::class)
	fun trigger() {
		val loc = findLocation()

		if (loc == null) {
			log.warn("Aborted spawning AI ship. Could not find location after 15 attempts.")
			return
		}
		val deferred = spawn(loc)

		deferred.invokeOnCompletion { throwable ->
			throwable?.let {
				IonServer.server.debug("AI Starship at could not be spawned: ${throwable.message}!")
				return@invokeOnCompletion
			}

			val ship = deferred.getCompleted()

			// Wait 1 tick for the controller to update
			Tasks.sync {
				AIManager.activeShips.add(ship)

				val spawnMessage = createSpawnMessage(
					ship.getDisplayNameComponent(),
					config.miniMessageSpawnMessage,
					Vec3i(loc),
					loc.world
				)

				if (IonServer.configuration.serverName == "Survival") Notify.online(spawnMessage) else IonServer.server.sendMessage(spawnMessage)
			}
		}
	}

	open fun createSpawnMessage(
		shipName: Component,
		message: String,
		location: Vec3i,
		world: World
	): Component {
		val (x, y, z) = location

		val formatted = message
			.replace("{x}", x.toString())
			.replace("{y}", y.toString())
			.replace("{z}", z.toString())
			.replace("{shipName}", miniMessage().serialize(shipName))

		return miniMessage().deserialize(formatted)
	}
}

class BasicCargoMissionSpawner : AISpawner("CARGO_MISSION") {
	override val config: AIShipConfiguration.AISpawnerConfiguration get() = IonServer.aiShipConfiguration.spawners.CARGO_MISSION

	override fun findLocation(): Location? {
		val worldConfig = config.worldWeightedRandomList.random()
		val world = worldConfig.getWorld()

		var iterations = 0

		val border = world.worldBorder
		val spawnRadius = (border.size / 2.0) * 0.9 // 90% of the way to world border
		val center = border.center

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val randomX = Random.nextDouble(-spawnRadius, +spawnRadius) + center.x
			val randomZ = Random.nextDouble(-spawnRadius, +spawnRadius) + center.z

			val origin = Vector(randomX, 192.0, randomZ)

			val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

			if (planets.any { it.distance(origin) < 500.0 }) continue

			return Location(world, randomX, 192.0, randomZ)
		}

		return null
	}

	private fun findEndpoint(origin: Location): Location? {
		var iterations = 0
		val (world, originX, originY, originZ) = origin

		while (iterations < 15) {
			iterations++

			val endPointX = if (originX > 0) Random.nextDouble(-originX, 0.0) else Random.nextDouble(0.0, -originX)
			val endPointZ = if (originZ > 0) Random.nextDouble(-originZ, 0.0) else Random.nextDouble(0.0, -originZ)
			val endPoint = Vector(endPointX, originY, endPointZ)

			val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

			val minDistance = planets.minOfOrNull {
				val direction = endPoint.clone().subtract(origin.toVector())

				distanceToVector(origin.toVector(), direction, it)
			}

			// If there are planets, and the distance to any of them along the path of travel is less than 500, discard
			if (minDistance != null && minDistance <= 500.0) continue

			return Location(world, endPointX, originY, endPointZ)
		}

		return null
	}

	override fun spawn(location: Location, callback: (ActiveControlledStarship) -> Unit): Deferred<ActiveControlledStarship> {
		val (ship, pilotName) = getTemplate(location.world)
		val deferred = CompletableDeferred<ActiveControlledStarship>()

		val controller: (ActiveControlledStarship) -> Controller = { starship: ActiveControlledStarship ->
			val factory = AIControllerFactories[ship.controllerFactory]
			val endpoint = findEndpoint(location)

			factory.createController(
				starship,
				pilotName,
				null,
				endpoint,
				ship.manualWeaponSets,
				ship.autoWeaponSets,
				null
			)
		}

		createAIShipFromTemplate(ship, location, controller, pilotName) {
			deferred.complete(it)
			callback(it)
		}

		return deferred
	}
}
