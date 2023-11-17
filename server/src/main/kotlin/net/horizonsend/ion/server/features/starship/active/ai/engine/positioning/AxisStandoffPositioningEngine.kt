package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.targeting.TargetingEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.nearestPointToVector
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.pow

/**
 * This positioning engine seeks out a position at the closest cardinal direction from the target
 * it has a variable holdoff distance
 **/
class AxisStandoffPositioningEngine(
	controller: AIController,
	var target: TargetingEngine,
	var standoffDistance: Double
) : PositioningEngine(controller) {
	val targetStandoffBonus = (target.findTarget() as? StarshipTarget)?.ship?.initialBlockCount?.toDouble()?.pow((1.0 / 3.0)) ?: 0.0
	val standoffBonus = controller.starship.initialBlockCount.toDouble().pow((1.0 / 3.0))
	private fun getAxisPoint(): Vector {
		target ?: return controller.getCenter().toVector()

		val shipLocation = getCenter().toVector()
		val targetLocation = getDestination().toVector()

		val vectors = CARDINAL_BLOCK_FACES.map {
			val vec = it.direction.multiply(standoffDistance + standoffBonus + targetStandoffBonus)
			nearestPointToVector(targetLocation, vec, shipLocation)
		}

		val axisPointFar = vectors.minBy {
			it.distance(shipLocation)
		}

		val cardinalOffsets = CARDINAL_BLOCK_FACES.map { it.direction.multiply(standoffDistance + standoffBonus + targetStandoffBonus) }
		val points = cardinalOffsets.map { targetLocation.clone().add(it) }

		val axisPointClose = points.minBy {
			it.distance(shipLocation)
		}

		return if (shipLocation.distanceSquared(axisPointFar) <= 100.0) axisPointClose else axisPointFar
	}

	override fun getDestination(): Vec3i = target.findTarget()?.getVec3i() ?: controller.starship.centerOfMass

	override fun findPosition(): Location {
		return getAxisPoint().toLocation(world)
	}

	override fun findPositionVec3i(): Vec3i {
		return Vec3i(getAxisPoint())
	}
}
