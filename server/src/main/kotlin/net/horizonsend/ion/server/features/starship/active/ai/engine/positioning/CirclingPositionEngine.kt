package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tries to position the ship in a circling pattern around the target
 **/
class CirclingPositionEngine(controller: AIController, var target: Vec3i?, var holdOffDistance: Double) : PositioningEngine(controller) {
	private val ticksPerCruise = StarshipCruising.SECONDS_PER_CRUISE * 20.0
	private var internalDestination: Vec3i? = target

	override fun findPosition(): Location = internalDestination?.toLocation(world) ?: getCenter()

	override fun findPositionVec3i(): Vec3i = internalDestination ?: getCenterVec3i()

	var ticks = 0

	/** Do the calculation on tick so its not done every time its called */
	override fun tick() {
		ticks++

		val target = target

		if (target == null) {
			internalDestination = null
			return
		}

		// Calculate the amount of ticks it would take to do a lap around the target
		val moveTicks = 1 / getMovePercent()

		// Convert to radians
		val radians = 2 * PI * (ticks / moveTicks)

		val x = ((cos(radians) * holdOffDistance) + target.x).toInt()
		val y = target.y
		val z = ((sin(radians) * holdOffDistance) + target.z).toInt()

		internalDestination = Vec3i(x, y, z)
	}

	private fun circleCircumference() = 2 * PI * holdOffDistance

	/** Returns the percentage the target location should move around the target per tick */
	private fun getMovePercent(): Double {
		starship as ActiveControlledStarship

		val circumference = circleCircumference()

		val cruiseSpeed = starship.cruiseData.targetSpeed.toDouble()
		val blocksPerTick = cruiseSpeed / ticksPerCruise

		return blocksPerTick/ circumference
	}

	override fun getDestination(): Vec3i = internalDestination ?: getCenterVec3i()
}
