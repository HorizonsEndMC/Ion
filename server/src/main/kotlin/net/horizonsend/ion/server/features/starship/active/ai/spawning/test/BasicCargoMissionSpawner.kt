package net.horizonsend.ion.server.features.starship.active.ai.spawning.test

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.templateMiniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getLocationNear
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getNonProtectedPlayer
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.random.Random

class BasicCargoMissionSpawner : AISpawner("CARGO_MISSION", IonServer.aiShipConfiguration.spawners::CARGO_MISSION) {
	fun findLocation(): Location? {
		// Get a random world based on the weight in the config
		val worldConfig = configuration.worldWeightedRandomList.random()
		val world = worldConfig.getWorld()

		val player = getNonProtectedPlayer(world) ?: return null

		var iterations = 0

		val border = world.worldBorder

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val loc = player.getLocationNear(minDistanceFromPlayer, maxDistanceFromPlayer)

			if (!border.isInside(loc)) continue

			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			return loc
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

	override fun spawningConditionsMet(world: World, x: Int, y: Int, z: Int): Boolean {
		return true // No restrictions
	}

	override suspend fun triggerSpawn() {
		val loc = findLocation() ?: return
		val (x, y, z) = Vec3i(loc)

		if (!spawningConditionsMet(loc.world, x, y, z)) return

		val (template, pilotName) = getStarshipTemplate(loc.world)

		val deferred = spawnAIStarship(template, loc, createController(template, pilotName))

		deferred.invokeOnCompletion {
			IonServer.server.sendMessage(templateMiniMessage(
				configuration.miniMessageSpawnMessage,
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				template.getName(),
				x,
				y,
				z,
				loc.world.name
			))
		}
	}

	override fun createController(template: AIShipConfiguration.AIStarshipTemplate, pilotName: Component): (ActiveStarship) -> Controller {
		val factory = AIControllerFactories[template.controllerFactory]

		return { starship ->
			val world = starship.world
			val center = world.worldBorder.center

			factory.createController(
				starship,
				pilotName,
				null,
				center,
				template.manualWeaponSets,
				template.autoWeaponSets,
				null // No previous
			)
		}
	}
}
