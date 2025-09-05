package net.horizonsend.ion.server.features.ai.configuration


import kotlinx.serialization.Serializable

@Serializable
data class AIEmities(
	val defaultAIEmityConfiguration: AIEmityConfiguration = AIEmityConfiguration()
) {

	@Serializable
	data class AIEmityConfiguration(
		val damagerWeight: Double = 1.0,
		val distanceWeight: Double = 100.0,
		val sizeWeight: Double = 0.75,
		val outOfRangeDecay: Double = 0.75,
		val outOfSystemDecay: Double = 0.01,
		val damagerDeacy: Double = 0.8,
		val aggroRange: Double = 1000.0,
		val initialAggroThreshold: Double = 1.0,
		val distanceAggroWeight: Double = 0.15,
		val gravityWellAggro: Double = 10.0,
		val damagerAggroWeight: Double = 2.1,
	)
}
