package net.horizonsend.ion.server.features.ai.configuration


import kotlinx.serialization.Serializable

@Serializable
data class AIEmities(
	val defaultAIEmityConfiguration: AIEmityConfiguration = AIEmityConfiguration()
) {

	@Serializable
	data class AIEmityConfiguration(
		val damagerWeight: Double = 1.0,
		val distanceWeight: Double = 3.0,
		val sizeWeight: Double = 4.0,
		val outOfRangeDecay: Double = 0.9,
		val outOfSystemDecay: Double = 0.1,
		val damagerDeacy: Double = 0.5,
		val aggroRange: Double = 700.0,
		val initialAggroThreshold: Double = 1.0,
		val distanceAggroWeight: Double = 1.0,
		val gravityWellAggro: Double = 10.0,
		val damagerAggroWeight: Double = 2.0,
	)
}
