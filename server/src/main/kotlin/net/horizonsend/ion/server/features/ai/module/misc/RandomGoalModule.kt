package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceToVector
import java.util.Optional
import kotlin.random.Random

class RandomGoalModule(controller: AIController) : AIModule(controller) {
	val maxIterations = 15
	var iterations = 0
	var ticks = 0
	val tickRate = 40
	var currentGoal: GoalTarget? = nextEndpoint() ?: GoalTarget(starship.centerOfMass, world, false, attack = false)
	val enmity: EnmityModule? get() = controller.getCoreModuleByType<EnmityModule>()

	override fun tick() {
		if (currentGoal == null) return
		if (enmity == null) return

		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0

		val currentLoc = starship.centerOfMass.toLocation(world)
		if (currentLoc.world == currentGoal!!.getWorld() &&
			currentLoc.distance(currentGoal!!.getLocation()) < 100.0
		) {

			val nextDestination = nextEndpoint()
			enmity!!.removeTarget(currentGoal!!)
			if (nextDestination != null) {
				enmity!!.addTarget(nextDestination, baseWeight = 0.5, aggroed = true, decay = false)
			}
			currentGoal = nextDestination
		}

	}

	fun nextEndpoint(): GoalTarget? {
		if (iterations == maxIterations) return null

		val origin = controller.getCenter()

		val world = controller.getWorld()
		val border = world.worldBorder

		val minX = (border.center.x - border.size).toInt()
		val minZ = (border.center.z - border.size).toInt()
		val maxX = (border.center.x + border.size).toInt()
		val maxZ = (border.center.z + border.size).toInt()

		val endPointX = Random.nextInt(minX, maxX)
		val endPointZ = Random.nextInt(minZ, maxZ)
		val endPoint = Vec3i(endPointX, origin.y, endPointZ)

		val planets = Space.getAllPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		val minDistance = planets.minOfOrNull {
			val direction = endPoint.minus(origin)

			distanceToVector(origin.toVector(), direction.toVector(), it)
		}

		// If there are planets, and the distance to any of them along the path of travel is less than 200, discard
		if (minDistance != null && minDistance <= 200.0) return nextEndpoint()
		iterations++
		return GoalTarget(endPoint, world, false, attack = false)

	}

	val cruiseEndpoint: (AIController) -> Optional<Vec3i> = lambda@{ controller ->
		var iterations = 0
		val origin = controller.getCenter()

		val world = controller.getWorld()
		val border = world.worldBorder

		val minX = (border.center.x - border.size).toInt()
		val minZ = (border.center.z - border.size).toInt()
		val maxX = (border.center.x + border.size).toInt()
		val maxZ = (border.center.z + border.size).toInt()

		while (iterations < 15) {
			iterations++

			val endPointX = Random.nextInt(minX, maxX)
			val endPointZ = Random.nextInt(minZ, maxZ)
			val endPoint = Vec3i(endPointX, origin.y, endPointZ)

			val planets = Space.getAllPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

			val minDistance = planets.minOfOrNull {
				val direction = endPoint.minus(origin)

				distanceToVector(origin.toVector(), direction.toVector(), it)
			}

			// If there are planets, and the distance to any of them along the path of travel is less than 500, discard
			if (minDistance != null && minDistance <= 500.0) continue

			return@lambda Optional.of(endPoint)
		}
		return@lambda Optional.empty<Vec3i>()
	}
}
