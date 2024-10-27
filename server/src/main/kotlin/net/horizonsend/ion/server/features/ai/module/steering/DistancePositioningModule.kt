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
	val minDist get() = config.minDist
	val maxDist get() = config.maxDist
	val optimalDist get() = config.optimalDist
	val distRange get() =  maxDist - minDist
	val optimalMapped get() = (optimalDist - minDist)/(distRange)
	val startFleeing get() = config.startFleeing
	val stopFleeing get() = config.stopFleeing

	var isFleeing = false

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
