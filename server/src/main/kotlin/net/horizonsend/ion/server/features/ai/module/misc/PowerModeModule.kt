package net.horizonsend.ion.server.features.ai.module.misc

import SteeringModule
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.AIPowerModes
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier
import kotlin.math.exp
import kotlin.math.pow

class PowerModeModule(
	controller: AIController,
	val difficulty: DifficultyModule,
	val generalTargetSupplier : Supplier<AITarget?>,
	val steeringModule: SteeringModule,
	val configSupplier: Supplier<AIPowerModes.AIPowerModeConfiguration> = Supplier(
		ConfigurationFiles.aiPowerModeConfiguration()::defaultAIPowerModeConfiguration),
) : AIModule(controller) {
    private val ship: Starship get() = controller.starship
	private val config get() = configSupplier.get()
	private val target : AITarget? get() = generalTargetSupplier.get()

	val tickRate = 10
	var ticks = 0 + randomInt(0,tickRate) //randomly offset powermode updates

	var currentPowerMode = PowerMode(0.4,0.4,0.2, false, true)


	override fun tick() {
		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0
		evaluateBestPowerMode()
	}

    fun evaluateBestPowerMode(useSoftMax : Boolean = false) {

		val finalPowerMode : PowerMode

		if (!difficulty.powerModeSwitch) {
			finalPowerMode = config.powermodes.first { it.base }
		} else if (useSoftMax) {
			val ratios : List<Double>
			if (difficulty.useSpecialPowerModes) {
				ratios = softmaxConstrained(listOf(shieldScore(),weaponsScore(),thrustScore()),0.0,0.6,1.1)
			} else {
				ratios = softmaxConstrained(listOf(shieldScore(),weaponsScore(),thrustScore()))
			}

			finalPowerMode = PowerMode(ratios[0],ratios[1],ratios[2],false, false)

		} else {
			val scored = config.powermodes.filter {
				difficulty.useSpecialPowerModes or !it.special }.map { score(it) to it }
			finalPowerMode = scored.maxByOrNull{it.first}!!.second  // there will always be one non-special powermode
		}

		if (currentPowerMode == finalPowerMode) return

		currentPowerMode = finalPowerMode

        ship.updatePower("AI", finalPowerMode.shield,finalPowerMode.weapons,finalPowerMode.thrust,
			difficulty.useSpecialPowerModes)
    }

	fun score(powerMode: PowerMode) : Double {
		return (shieldScore()* powerMode.shield
			+ weaponsScore() * powerMode.weapons
			+ thrustScore() * powerMode.thrust)
	}


	fun shieldScore() : Double {
		return (
			config.baseShieldScore
			//raise on critical shield
			+ (if (ship.shields.isEmpty()) 0.0 else 1 - ship.shields.map{it.powerRatio}.average()).pow(3)
				* config.criticalShieldMultiplier
			//increase on distance
			+ (((target?.getLocation()?.toVector()?.distance(location.toVector()) ?: 500.0))/500.0).coerceIn(0.0,1.0)
				* config.shieldDistanceMultiplier
			)
	}

	fun weaponsScore() : Double {
		return (
			config.baseWeaponsScore
			//discount based on distance
			- (((target?.getLocation()?.toVector()?.distance(location.toVector()) ?: 500.0))- 500.0/500.0).coerceIn(0.0,1.0)
				* config.weaponsDistanceMultiplier
			)

	}


	fun thrustScore() : Double {
		return (
			config.baseThrustScore
			+ speedScore() * config.thrustSpeedMultiplier
			+ directionScore() * config.thrustDirectionMultiplier
			- driftScore() * config.thrustDriftMultiplier
			)
	}

	fun speedScore() : Double {
		//might need to account for throttle later
		val speedRatio = 1 - (ship.velocity.length() / controller.maxSpeed).coerceIn(0.0,1.0)
		return speedRatio.pow(config.thrustSpeedPower)
	}

	fun directionScore() : Double {
		val velocity = ship.velocity.clone()
		if (velocity.lengthSquared() > 1e-4) velocity.normalize()
		val dot = ship.velocity.dot(steeringModule.thrustOut)
		return (dot + 1)/2.0
	}

	fun driftScore() : Double {
		val dot = steeringModule.headingOut.dot(steeringModule.thrustOut)
		return (-dot + 0.2).coerceIn(0.0,2.0)

	}


	fun softmaxConstrained(scores: List<Double>, min: Double = 0.1, max: Double = 0.5, total : Double = 1.0): List<Double> {
		// Step 1: Apply softmax
		val expScores = scores.map { exp(it) }
		val sumExpScores = expScores.sum()
		val softmax = expScores.map { it / sumExpScores }

		// Step 2: Rescale softmax to the target range [min, max]
		val minValue = softmax.minOrNull() ?: 0.0
		val maxValue = softmax.maxOrNull() ?: 1.0

		val rescaled = softmax.map { s ->
			min + (max - min) * (s - minValue) / (maxValue - minValue)
		}

		// Step 3: Renormalize to ensure the sum is total
		val sumRescaled = rescaled.sum()
		return rescaled.map { (it / sumRescaled) * total }
	}


	@Serializable
	data class PowerMode(val shield : Double, val weapons : Double, val thrust : Double,
						 val special : Boolean, val base : Boolean) {}

}
