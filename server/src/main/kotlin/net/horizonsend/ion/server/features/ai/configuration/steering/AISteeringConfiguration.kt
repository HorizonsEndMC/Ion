package net.horizonsend.ion.server.features.ai.configuration.steering

import kotlinx.serialization.Serializable
@Serializable
data class AISteeringConfiguration(
	val defaultBasicSteeringConfiguration : BasicSteeringConfiguration = BasicSteeringConfiguration()
) {
	@Serializable
	data class BasicSteeringConfiguration(
		val defaultMaxSpeed : Double = 20.0,
		val defaultRotationContribution : Double = 0.2,
		val defaultRotationMixingRatio : Double = 0.2,
		val defaultRotationMixingPower : Double = 0.5
	)

}
