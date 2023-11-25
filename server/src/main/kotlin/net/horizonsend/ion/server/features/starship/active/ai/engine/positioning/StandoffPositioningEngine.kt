package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.pow

class StandoffPositioningEngine(
	controller: AIController,
	var targetSupplier: Supplier<AITarget?>,
	var standoffDistance: Double
) : PositioningEngine(controller) {
	private val targetStandoffBonus get() = (targetSupplier.get() as? StarshipTarget)?.ship?.initialBlockCount?.toDouble()?.pow((1.0 / 3.0)) ?: 0.0
	private val standoffBonus = controller.starship.initialBlockCount.toDouble().pow((1.0 / 3.0))

	private val standoffRange get() = standoffDistance + standoffBonus + targetStandoffBonus

	override fun findPosition(): Location {
		val target = targetSupplier.get() ?: return getCenter()

		val vector = targetToShipVector(target)

		return target.getLocation().add(vector.multiply(standoffRange))
	}

	override fun findPositionVec3i(): Vec3i {
		return Vec3i(findPosition())
	}

	override fun getDestination(): Vec3i {
		return targetSupplier.get()?.getVec3i() ?: return Vec3i(getCenter())
	}

	/** Get a normalized vector from the target to the ship */
	private fun targetToShipVector(target: AITarget): Vector {
		val center = getCenterVec3i().toVector()
		val targetPosition = target.getLocation().toVector()

		return targetPosition.subtract(center).normalize()
	}

	override fun toString(): String {
		return "StandoffPositioningEngine[Standoff: $standoffDistance, $standoffRange]"
	}
}
