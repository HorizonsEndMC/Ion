package net.horizonsend.ion.server.features.ai.module.steering

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.steering.AISteeringConfiguration
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier
import kotlin.math.ln
import kotlin.math.max

class DistancePositioningModule(
	controller: AIController,
	val difficulty : DifficultyModule,
	val generalTarget : Supplier<AITarget?>,
	val configSupplier: Supplier<AISteeringConfiguration.DistanceConfiguration>
) : AIModule(controller){
	val ship get() = controller.starship
	val config : AISteeringConfiguration.DistanceConfiguration get() = configSupplier.get()
	private val minDist get() = config.minDist
	private val maxDist get() = config.maxDist
	private val fleeDist get() = config.fleeDist
	private val optimalDist get() = config.optimalDist
	private val startFleeing get() = config.startFleeing
	private val stopFleeing get() = config.stopFleeing

	private var isFleeing = false

	fun calcDistance() : Double {
		if (!difficulty.doBackOff) return calcCombatDist()

		if (controller.getMinimumShieldHealth() <= startFleeing) {
			isFleeing = true
		}
		if (controller.getMinimumShieldHealth() >= stopFleeing) {
			isFleeing = false
		}

		return if (isFleeing) fleeDist else calcCombatDist()
	}

	private fun calcCombatDist() : Double{
		val target = generalTarget.get()
		if (target is GoalTarget) return 0.1
		if (target !is StarshipTarget) return optimalDist
		val blockRatio = target.ship.initialBlockCount.toDouble() / starship.initialBlockCount.toDouble()
		val optimalDist =  distanceFromRatio(blockRatio,minDist,optimalDist,maxDist)
		//renegotiate the distance if the opponent is playing very aggressively
		// Compute vector from center to ship (flattened)
		val toShip = target.ship.centerOfMass.toVector().subtract(getCenter().toVector())
		toShip.y = 0.0
		val length = toShip.length()
		if (length >= optimalDist) return optimalDist
		val adjustedRatio = (blockRatio * config.agressionTolerance).coerceIn(0.0,1.0)
		val minTolerableDist = optimalDist * adjustedRatio
		return max(minTolerableDist, length)
	}

	private fun distanceFromRatio(r: Double, dMin: Double, dOpt: Double, dMax: Double): Double {
		// Clamp ratio within valid range to avoid log errors
		val rMin = 0.025
		val rMax = 40.0

		val clampedR = r.coerceIn(rMin,rMax)

		// Compute logarithms safely
		val logR = ln(clampedR)
		val logRMin = ln(rMin)
		val logRMax = ln(rMax)

		val log1 = 0.0  // ln(1) is always 0

		// Compute scaling factors
		val S1 = if (clampedR > rMin) (logR - logRMin) / (log1 - logRMin) else 0.0
		val S2 = if (clampedR > 1.0) (logR - log1) / (logRMax - log1) else 0.0

		// Ensure S1 and S2 stay within [0,1]
		val S1Clamped = S1.coerceIn(0.0,1.0)
		val S2Clamped = S2.coerceIn(0.0,1.0)

		// Compute the final distance
		return dMin + (dOpt - dMin) * S1Clamped + (dMax - dOpt) * S2Clamped
	}

}
