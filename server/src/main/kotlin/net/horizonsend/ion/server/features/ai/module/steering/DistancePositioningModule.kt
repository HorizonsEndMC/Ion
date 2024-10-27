package net.horizonsend.ion.server.features.ai.module.steering

import net.horizonsend.ion.server.features.ai.configuration.steering.AISteeringConfiguration
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class DistancePositioningModule(
	controller: AIController,
	val difficulty : DifficultyModule,
	val config : AISteeringConfiguration.DistanceConfiguration
) : AIModule(controller){
	val ship get() = controller.starship
	private val minDist get() = config.minDist
	private val maxDist get() = config.maxDist
	private val optimalDist get() = config.optimalDist
	private val distRange get() =  maxDist - minDist
	private val optimalMapped get() = (optimalDist - minDist) / distRange
	private val startFleeing get() = config.startFleeing
	private val stopFleeing get() = config.stopFleeing

	private var isFleeing = false

	fun calcDistance() : Double {
		if (!difficulty.doBackOff) return optimalDist

		if (controller.getMinimumShieldHealth() <= startFleeing) {
			isFleeing = true
		}
		if (controller.getMinimumShieldHealth() >= stopFleeing) {
			isFleeing = false
		}

		return if (isFleeing) maxDist else optimalDist
	}

}
