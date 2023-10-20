package net.horizonsend.ion.server.features.starship.active.ai.spawning

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.common.utils.text.isVowel
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIManager
import net.horizonsend.ion.server.features.starship.active.ai.AIStarshipFactory.createAIShipFromTemplate
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.TemporaryStarfighterCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation.AutoCruiseAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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

	// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
	open fun getTemplate(world: World): AIStarshipTemplate? {
		val shipID = config.getWorld(world)?.shipsWeightedList?.randomOrNull() ?: return null
		return IonServer.aiShipConfiguration.getShipTemplate(shipID)
	}

	open val createController: (ActiveStarship) -> Controller = { NoOpController(it, null) }

	open fun spawn(location: Location, callback: (ActiveControlledStarship) -> Unit = {}): Deferred<ActiveControlledStarship> {
		val ship = getTemplate(location.world)!!
		val deferred = CompletableDeferred<ActiveControlledStarship>()

        createAIShipFromTemplate(ship, location, createController) {
            deferred.complete(it)
            callback(it)
        }

		return deferred
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun handleSpawn() {
		val loc = findLocation()

		if (loc == null) {
			log.warn("Aborted spawning AI ship. Could not find location after 15 attempts.")
			return
		}
		val deferred = spawn(loc)

		deferred.invokeOnCompletion { throwable ->
			println("completed")
			throwable?.let {
				IonServer.server.debug("AI Starship at could not be spawned: ${throwable.message}!")
				return@invokeOnCompletion
			}

			val ship = deferred.getCompleted()

			// Wait 1 tick for the controller to update
			Tasks.sync {
				val controller = ship.controller as AIController

				AIManager.activeShips.add(ship)

				val spawnMessage = createSpawnMessage(
					controller.aggressivenessLevel,
					ship.getDisplayNameComponent(),
					Vec3i(loc),
					loc.world
				)

				if (IonServer.configuration.serverName == "Survival") Notify.online(spawnMessage) else IonServer.server.sendMessage(spawnMessage)
			}
		}
	}

	open fun createSpawnMessage(
		aggressivenessLevel: AggressivenessLevel,
		shipName: Component,
		location: Vec3i,
		world: World
	): Component {
		val aAn = if (aggressivenessLevel.name[0].isVowel()) "An " else "A "

		val (x, y, z) = location

		return Component.text()
			.color(NamedTextColor.GRAY)
			.append(Component.text(aAn))
			.append(aggressivenessLevel.displayName)
			.append(Component.text(" "))
			.append(shipName)
			.append(Component.text(" has spawned in "))
			.append(Component.text(world.name, NamedTextColor.WHITE))
			.append(Component.text(" at "))
			.append(Component.text(x, NamedTextColor.WHITE))
			.append(Component.text(", "))
			.append(Component.text(y, NamedTextColor.WHITE))
			.append(Component.text(", "))
			.append(Component.text(z, NamedTextColor.WHITE))
			.build()
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
		return super.spawn(location) callback@{
			val endpoint = findEndpoint(location) ?: return@callback

			val aggressivenessLevel = AggressivenessLevel.values().random()

			it.controller = AutoCruiseAIController(it, endpoint, 5, aggressivenessLevel) { controller, nearbyShip ->
				TemporaryStarfighterCombatAIController(controller.starship, nearbyShip, controller.aggressivenessLevel, controller)
			}
		}
	}
}
