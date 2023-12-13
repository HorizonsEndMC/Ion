package net.horizonsend.ion.server.features.starship.active.ai.spawning.template

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getLocationNear
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getNonProtectedPlayer
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.random.Random

class BasicCargoMissionSpawner : BasicSpawner("CARGO_MISSION", IonServer.aiShipConfiguration.spawners::CARGO_MISSION) {
	override fun findSpawnLocation(): Location? {
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
}
