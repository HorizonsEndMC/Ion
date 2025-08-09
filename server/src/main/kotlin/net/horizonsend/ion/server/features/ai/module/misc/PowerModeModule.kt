package net.horizonsend.ion.server.features.ai.module.misc

import SteeringModule
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.AIPowerModes
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.math.pow

class PowerModeModule(
	controller: AIController,
	val difficulty: DifficultyModule,
	val generalTargetSupplier: Supplier<AITarget?>,
	val steeringModule: SteeringModule,
	val configSupplier: Supplier<AIPowerModes.AIPowerModeConfiguration> = Supplier {
		ConfigurationFiles.aiPowerModeConfiguration().defaultAIPowerModeConfiguration
	},
) : AIModule(controller) {
	private val ship: Starship get() = controller.starship
	private val config get() = configSupplier.get()
	private val target: AITarget? get() = generalTargetSupplier.get()

	private val tickRate = 5
	private var ticks = 0 + randomInt(0, tickRate) //randomly offset powermode updates
	private var lastUpdate = System.currentTimeMillis()

	private var currentPowerMode = PowerMode(0.3, 0.3, 0.4, false, true)


	override fun tick() {
		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0
		evaluateBestPowerMode()
	}

	private fun evaluateBestPowerMode(useSoftMax: Boolean = true) {

		val finalPowerMode: PowerMode

		if (!difficulty.powerModeSwitch) {
			finalPowerMode = config.powermodes.first { it.base }
		} else if (useSoftMax) {
			val ratios: List<Double>
			//ship.debug("Shield score : ${shieldScore()},Weapon score : ${weaponsScore()},Thrust score : ${thrustScore()}")
			if (difficulty.useSpecialPowerModes) {
				ratios = distributeScores(listOf(shieldScore(), weaponsScore(), thrustScore()), 0.0, 0.6, 1.1)
			} else {
				ratios = distributeScores(listOf(shieldScore(), weaponsScore(), thrustScore()))
			}
			//ship.debug("ratios : $ratios")
			finalPowerMode = PowerMode(ratios[0], ratios[1], ratios[2], false, false)

		} else {
			val scored = config.powermodes.filter {
				difficulty.useSpecialPowerModes or !it.special
			}.map { score(it) to it }
			finalPowerMode = scored.maxByOrNull { it.first }!!.second  // there will always be one non-special powermode
		}

		if (currentPowerMode == finalPowerMode) return

		if (!shouldUpdatePowermode(finalPowerMode)) return

		lastUpdate = System.currentTimeMillis()
		currentPowerMode = finalPowerMode

		ship.updatePower(
			"AI", finalPowerMode.shield, finalPowerMode.weapons, finalPowerMode.thrust,
			difficulty.useSpecialPowerModes
		)
	}

	private fun score(powerMode: PowerMode): Double {
		return (shieldScore() * powerMode.shield
			+ weaponsScore() * powerMode.weapons
			+ thrustScore() * powerMode.thrust)
	}


	private fun shieldScore(): Double {
		return (
			config.baseShieldScore
				//raise on critical shield
				+ (if (ship.shields.isEmpty()) 0.0 else 1 - ship.shields.map { it.powerRatio }.average()).pow(3)
				* config.criticalShieldMultiplier
				//increase on distance
				+ (((target?.getLocation()?.toVector()?.distance(location.toVector()) ?: 500.0)) / 500.0).coerceIn(0.0, 1.0)
				* config.shieldDistanceMultiplier
			)
	}

	private fun weaponsScore(): Double {
		return (
			config.baseWeaponsScore
				//discount based on distance
				- (((target?.getLocation()?.toVector()?.distance(location.toVector()) ?: 500.0)) / 500.0).coerceIn(0.0, 1.0)
				.pow(0.5) * config.weaponsDistanceMultiplier
			).coerceAtLeast(0.0)

	}


	private fun thrustScore(): Double {
		val result = (
			config.baseThrustScore
				+ speedScore() * config.thrustSpeedMultiplier
				+ directionScore() * config.thrustDirectionMultiplier
				+ driftScore() * config.thrustDriftMultiplier
			).coerceAtLeast(0.0)

		if (result == 0.0) {
			ship.debug("base thrust: ${config.baseThrustScore}")
			ship.debug("Speed : ${speedScore()} * ${config.thrustSpeedMultiplier}")
			ship.debug("Direction : ${directionScore()} * ${config.thrustDirectionMultiplier}")
			ship.debug("Drift : ${driftScore()} * ${config.thrustDriftMultiplier}")
		}
		return result
	}

	private fun speedScore(): Double {
		//might need to account for throttle later
		val speedRatio = 1 - (ship.velocity.length() / (controller.maxSpeed * 1.1)).coerceIn(0.0, 1.0)
		return speedRatio.pow(config.thrustSpeedPower)
	}

	private fun directionScore(): Double {
		val velocity = ship.velocity.clone()
		if (velocity.lengthSquared() > 1e-4) velocity.normalize()
		val dot = velocity.dot(steeringModule.thrustOut)
		return (-dot + 1) / 2.0
	}

	private fun driftScore(): Double {
		val dot = steeringModule.headingOut.dot(steeringModule.thrustOut)
		return (dot - 0.3).coerceIn(-2.0, 0.0)

	}

	private fun distributeScores(
		scores: List<Double>,
		minValue: Double = 0.1,
		maxValue: Double = 0.5,
		targetSum: Double = 1.0
	): List<Double> {
		// Step 1: Normalize the scores to match the target sum
		val totalScore = scores.sum()
		var adjustedScores = scores.map { it / totalScore * targetSum }.toMutableList()

		// Step 2: Apply lower and upper bounds
		var deficit = 0.0
		var excess = 0.0

		for (i in adjustedScores.indices) {
			if (adjustedScores[i] < minValue) {
				deficit += minValue - adjustedScores[i]
				adjustedScores[i] = minValue
			} else if (adjustedScores[i] > maxValue) {
				excess += adjustedScores[i] - maxValue
				adjustedScores[i] = maxValue
			}
		}

		// Step 3: Adjust remaining sum to match the target sum
		var total = adjustedScores.sum()

		if (total != targetSum) {
			val adjustableIndices = adjustedScores.indices.filter { adjustedScores[it] > minValue && adjustedScores[it] < maxValue }
			if (adjustableIndices.isNotEmpty()) {
				val adjustment = (targetSum - total) / adjustableIndices.size
				for (index in adjustableIndices) {
					adjustedScores[index] = (adjustedScores[index] + adjustment).coerceIn(minValue, maxValue)
				}
			}
		}

		return adjustedScores
	}

	private fun evaluateDifference(other : PowerMode) : Double {
		val shieldDiff = abs(currentPowerMode.shield - other.shield)
		val weaponDiff = abs(currentPowerMode.weapons - other.weapons)
		val thrustDiff = abs(currentPowerMode.thrust - other.thrust)

		return shieldDiff + weaponDiff + thrustDiff
	}

	private fun shouldUpdatePowermode(other: PowerMode) : Boolean {
		val diffMultiplier = (1.0 - evaluateDifference(other)).coerceAtLeast(0.0) //~0.2 for large shifts
		val sizeMultiplier = ship.initialBlockCount.toDouble().pow(1.0/3.0) // ~7 for 350s
		val cooldown = (250.0 * diffMultiplier * sizeMultiplier * difficulty.powerModeDelayMulti).toLong()

		val currentTime = System.currentTimeMillis()

		return (currentTime - lastUpdate) >= cooldown
	}


	@Serializable
	data class PowerMode(
		val shield: Double, val weapons: Double, val thrust: Double,
		val special: Boolean, val base: Boolean
	)

}
