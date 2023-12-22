package net.horizonsend.ion.server.features.starship.ai.module.positioning

import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.nearestPointToVector
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.pow

/**
 * This positioning module seeks out a position at the closest cardinal direction from the target
 * it has a variable holdoff distance
 **/
class AxisStandoffPositioningModule(
	controller: AIController,
	var target: Supplier<AITarget?>,
	var standoffDistance: Double
) : PositioningModule(controller) {
	val targetStandoffBonus = (target.get() as? StarshipTarget)?.ship?.initialBlockCount?.toDouble()?.pow((1.0 / 3.0)) ?: 0.0
	val standoffBonus = controller.starship.initialBlockCount.toDouble().pow((1.0 / 3.0))

	private fun getAxisPoint(): Vector {
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

	override fun getDestination(): Vec3i = target.get()?.getVec3i() ?: controller.starship.centerOfMass

	override fun findPosition(): Location {
		return getAxisPoint().toLocation(world)
	}

	override fun findPositionVec3i(): Vec3i {
		return Vec3i(getAxisPoint())
	}
}
