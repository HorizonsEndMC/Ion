package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.util.Vector

/**
 * This positioning engine seeks out a position at the closest cardinal direction from the target
 * it has a variable holdoff distance
 **/
class AxisStandoffPositioningEngine(
	controller: AIController,
	var target: ActiveStarship?,
	var standoffDistance: Double
) : PositioningEngine(controller) {
	fun getAxisPoint(): Vector {
		val target = target ?: return controller.getCenter().toVector()

		val shipLocation = getCenter().toVector()
		val targetLocation = target.centerOfMass.toVector()

		val cardinalOffsets = CARDINAL_BLOCK_FACES.map { it.direction.multiply(standoffDistance) }
		val points = cardinalOffsets.map { targetLocation.clone().add(it) }

		return points.minBy { it.distance(shipLocation) }
	}

	override fun findPosition(): Location {
		return getAxisPoint().toLocation(world)
	}

	override fun findPositionVec3i(): Vec3i {
		return Vec3i(getAxisPoint())
	}
}
