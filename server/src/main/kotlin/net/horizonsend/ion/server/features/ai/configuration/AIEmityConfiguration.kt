package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection

@Serializable
data class AIEmities(
	val defaultAIEmityConfiguration: AIEmityConfiguration = AIEmityConfiguration()
) {

	@Serializable
	data class AIEmityConfiguration(
		val damagerWeight : Double = 1.0,
		val distanceWeight : Double = 1.0,
		val sizeWeight : Double = 1.0,
		val outOfRangeDecay : Double = 0.9,
		val outOfSystemDecay : Double = 0.1,
		val aggroRange : Double = 500.0,
		val initialAggroThreshold : Double = 1.0,
		val distanceAggroWeight : Double = 1.0,
		val gravityWellAggro : Double = 10.0,
		val damagerAggroWeight : Double = 10.0,
		val defaultEmityFilter : (@Contextual AITarget) -> Boolean = fun(target : AITarget) : Boolean {
			if (target is PlayerTarget) {
				return target.player.hasProtection()
			}
			if (target is StarshipTarget) {
				return target.ship.playerPilot?.hasProtection() ?: false
			}
			return false
		}
	)
}
