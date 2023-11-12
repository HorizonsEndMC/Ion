package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.nearestPointToVector
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class RotatingAxisStandoffPositioningEngine(
	controller: ActiveAIController,
	var target: AITarget?,
	var standoffDistance: Double,
	val faces: List<BlockFace>
) : PositioningEngine(controller) {
	var loopSize = 60 * 20

	var ticks = 0

	override fun tick() {
		if (ticks == loopSize) {
			ticks = 0
			return
		}

		ticks++
	}

	fun getBlockFace(): BlockFace {
		val progress = loopSize.toDouble() / ticks.toDouble()
		val newIndex = minOf((progress * faces.lastIndex).toInt(), faces.lastIndex)

		return faces[newIndex]
	}

	fun getAxisPoint(): Vector {
		target ?: return controller.getCenter().toVector()

		val face = getBlockFace()

		val shipLocation = getCenter().toVector()
		val targetLocation = getDestination().toVector()

		val vectorFar = face.direction.multiply(500.0)
		val nearest = nearestPointToVector(targetLocation, vectorFar, shipLocation)

		val goal = shipLocation.clone().add(face.direction.multiply(standoffDistance))

		return if (shipLocation.distanceSquared(nearest) <= 100.0) goal else nearest
	}

	override fun getDestination(): Vec3i = target?.getVec3i() ?: controller.starship.centerOfMass

	override fun findPosition(): Location {
		return getAxisPoint().toLocation(world)
	}

	override fun findPositionVec3i(): Vec3i {
		return Vec3i(getAxisPoint())
	}
}
