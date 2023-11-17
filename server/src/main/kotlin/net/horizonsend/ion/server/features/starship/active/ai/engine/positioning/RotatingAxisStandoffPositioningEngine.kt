package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.nearestPointToVector
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.pow

class RotatingAxisStandoffPositioningEngine(
	controller: AIController,
	var target: AITarget?,
	var standoffDistance: Double,
	val faces: List<BlockFace>
) : PositioningEngine(controller) {
	val standoffBonus = controller.starship.initialBlockCount.toDouble().pow((1.0 / 3.0))
	var loopSize = 60 * 20

	var ticks = 0

	override fun tick() {
		if (ticks >= loopSize) {
			ticks = 0
			return
		}

		ticks++
	}

	fun getBlockFace(): BlockFace {
		val progress = ticks.toDouble() / loopSize.toDouble()
		val newIndex = minOf((progress * (faces.size - 1)).toInt(), (faces.size - 1))

		return faces[newIndex]
	}

	fun getAxisPoint(): Vector {
		target ?: return controller.getCenter().toVector()

		val face = getBlockFace()

		val shipLocation = getCenter().toVector()
		val targetLocation = getDestination().toVector()

		val vectorFar = face.direction.multiply(standoffDistance + standoffBonus)
		val nearest = nearestPointToVector(targetLocation, vectorFar, shipLocation)

		val goal = shipLocation.clone().add(face.direction.multiply(standoffDistance + standoffBonus))

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
