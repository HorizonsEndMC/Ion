package net.horizonsend.ion.server.features.starship.ai.module.positioning

import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.pow

class StandoffPositioningModule(
	controller: AIController,
	var targetSupplier: Supplier<AITarget?>,
	var standoffDistance: Double
) : PositioningModule(controller) {
	private val targetStandoffBonus get() = (targetSupplier.get() as? StarshipTarget)?.ship?.initialBlockCount?.toDouble()?.pow((1.0 / 3.0)) ?: 0.0
	private val standoffBonus = controller.starship.initialBlockCount.toDouble().pow((1.0 / 3.0))

	private val standoffRange get() = standoffDistance + standoffBonus + targetStandoffBonus

	override fun findPosition(): Vec3i? {
		val target = targetSupplier.get() ?: return null

		val vector = targetToShipVector(target)

		return Vec3i(target.getLocation().add(vector.multiply(standoffRange)))
	}

	override fun getDestination(): Vec3i? {
		return targetSupplier.get()?.getVec3i()
	}

	/** Get a normalized vector from the target to the ship */
	private fun targetToShipVector(target: AITarget): Vector {
		val center = getCenterVec3i().toVector()
		val targetPosition = target.getLocation().toVector()

		return targetPosition.subtract(center).normalize()
	}

	override fun toString(): String {
		return "StandoffPositioningModule[Standoff: $standoffDistance, $standoffRange]"
	}
}
