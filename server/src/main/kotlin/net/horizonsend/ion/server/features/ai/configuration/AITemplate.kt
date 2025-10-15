package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.util.IntegerAmount
import net.horizonsend.ion.server.features.ai.reward.AICreditRewardProvider
import net.horizonsend.ion.server.features.ai.reward.AIItemBagRewardProvider
import net.horizonsend.ion.server.features.ai.reward.AIItemRewardProvider
import net.horizonsend.ion.server.features.ai.reward.AIKillStreakRewardProvider
import net.horizonsend.ion.server.features.ai.reward.AIXPRewardProvider
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.starship.BehaviorConfiguration
import net.horizonsend.ion.server.features.ai.starship.StarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.features.world.WorldSettings

@Serializable
data class AITemplate(
	val identifier: String,
	val starshipInfo: StarshipTemplate,
	val behaviorInformation: BehaviorConfiguration,
	val rewardProviders: List<AIRewardsProviderConfiguration>,
	val difficulty: IntegerAmount
) {
	@Serializable
	data class SpawningInformationHolder(
		val template: SpawnedShip,
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
		val items: List<WorldSettings.DroppedItem>
	) : AIRewardsProviderConfiguration {
		override fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider {
			return AIItemRewardProvider(starship, this)
		}
	}

	@Serializable
	data class ItemBagRewardProviderConfiguration(
		val items: List<WorldSettings.DroppedItem>,
		/** Max bag size for a damager with 1.0 score (100% damage at hard)*/
		val maxBagSize : Double,
		/** How small the bag can be once the max bagsize is evaluated*/
		val minBagModifier : Double,
	) : AIRewardsProviderConfiguration {
		override fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider {
			return AIItemBagRewardProvider(starship, this)
		}
	}

	@Serializable
	data class KillStreakRewardProviderConfiguration(
		val streakMultiplier: Double
	) : AIRewardsProviderConfiguration {
		override fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider {
			return AIKillStreakRewardProvider(starship, this)
		}
	}

	@Serializable
	sealed interface AIRewardsProviderConfiguration {
		fun createRewardsProvider(starship: ActiveControlledStarship, template: AITemplate): RewardsProvider
	}
}
