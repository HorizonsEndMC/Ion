package net.horizonsend.ion.server.features.starship.active.ai.spawning

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIStarshipFactory.createAIShipFromTemplate
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.combat.TemporaryStarfighterCombatAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.navigation.AutoCruiseAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.random.Random

abstract class AISpawner(val identifier: String) {
	val config: AIShipConfiguration.AISpawnerConfiguration get() =
		IonServer.aiShipConfiguration.spawners
			.firstOrNull { it.identifier == identifier } ?: throw IllegalStateException("AI Spawner $identifier does not have configuration!")

	abstract fun findLocation(): Location?

	// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
	open fun getTemplate(world: World): AIStarshipTemplate? {
		val shipID = config.getWorld(world)?.shipsWeightedList?.randomOrNull() ?: return null
		return IonServer.aiShipConfiguration.getShipTemplate(shipID)
	}

	open val createController: (ActiveStarship) -> Controller = { NoOpController(it) }

	open fun spawn(location: Location, callback: (ActiveControlledStarship) -> Unit = {}): Deferred<ActiveControlledStarship> {
		val ship = getTemplate(location.world)!!
		val deferred = CompletableDeferred<ActiveControlledStarship>()

        createAIShipFromTemplate(ship, location, createController) {
            deferred.complete(it)
            callback(it)
        }

		return deferred
	}
}

class BasicCargoMissionSpawner : AISpawner("CARGO_MISSION") {
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
