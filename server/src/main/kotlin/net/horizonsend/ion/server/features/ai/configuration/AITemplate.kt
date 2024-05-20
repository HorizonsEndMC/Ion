package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.ai.reward.AICreditRewardProvider
import net.horizonsend.ion.server.features.ai.reward.AIItemRewardProvider
import net.horizonsend.ion.server.features.ai.reward.AIXPRewardProvider
import net.horizonsend.ion.server.features.ai.starship.BehaviorConfiguration
import net.horizonsend.ion.server.features.ai.starship.StarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider

@Serializable
data class AITemplate(
	val identifier: String,
	val starshipInfo: StarshipTemplate,
	val behaviorInformation: BehaviorConfiguration,
	val rewardProviders: List<AIRewardsProviderConfiguration>
) {
	@Serializable
	data class SpawningInformationHolder(
		val template: AITemplate,
		val probability: Double
	)

	@Serializable
	data class CreditRewardProviderConfiguration(
		val creditReward: Double
	) : AIRewardsProviderConfiguration {
		override fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider {
			return AICreditRewardProvider(starship, this)
		}
	}

	@Serializable
	data class SLXPRewardProviderConfiguration(
		val xpMultiplier: Double
	) : AIRewardsProviderConfiguration {
		override fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider {
			return AIXPRewardProvider(starship, this)
		}
	}

	@Serializable
	data class ItemRewardProviderConfiguration(
		val items: List<ServerConfiguration.PlanetSpawnConfig.DroppedItem>
	) : AIRewardsProviderConfiguration {
		override fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider {
			return AIItemRewardProvider(starship, this)
		}
	}

	@Serializable
	sealed interface AIRewardsProviderConfiguration {
		fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider
	}
}
