package net.horizonsend.ion.server.features.ai.module.positioning

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tries to position the ship in a circling pattern around the target
 **/
class CirclingPositionModule(
	controller: AIController,
	var targetSupplier: Supplier<AITarget?>,
	var standoffDistance: Double
) : PositioningModule(controller) {
	private val ticksPerCruise = StarshipCruising.SECONDS_PER_CRUISE * 20.0
	private var internalDestination: Vec3i? = targetSupplier.get()?.getVec3i()

	override fun findPosition(): Vec3i? = internalDestination

	var ticks = 0

	/** Do the calculation on tick so its not done every time its called */
	override fun tick() {
		ticks++

		val target = targetSupplier.get()

		if (target == null) {
			internalDestination = null
			return
		}

		val (targetX, targetY, targetZ) = target.getVec3i()

		// Calculate the amount of ticks it would take to do a lap around the target
		val moveTicks = 1 / getMovePercent()

		// Convert to radians
		val radians = 2 * PI * (ticks / moveTicks)

		val x = ((cos(radians) * standoffDistance) + targetX).toInt()
		val y = targetY
		val z = ((sin(radians) * standoffDistance) + targetZ).toInt()

		internalDestination = Vec3i(x, y, z)
	}

	private fun circleCircumference() = 2 * PI * standoffDistance

	/** Returns the percentage the target location should move around the target per tick */
	private fun getMovePercent(): Double {
		val circumference = circleCircumference()

		val cruiseSpeed = (starship as ActiveControlledStarship).cruiseData.targetSpeed.toDouble()
		val blocksPerTick = cruiseSpeed / ticksPerCruise

		return blocksPerTick/ circumference
	}

	override fun getDestination(): Vec3i? = internalDestination
}
