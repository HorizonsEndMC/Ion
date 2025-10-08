package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.server.command.GlobalCompletions.toItemString
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.nations.sieges.SolarSieges.config
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

object SolarSiegeRewards {
	private val SOLAR_SIEGE_REWARDS_BAG: RewardsBag = RewardsBag.builder()
		.addReward(Material.CYAN_TERRACOTTA, 1, 1)
		.build()

	fun generateRewards(attackerPoints: Int): Map<String, Int> {
		val rewardScale = minOf(attackerPoints, config.rewardPointCap).toDouble() / config.rewardPointCap.toDouble()

		val points = (config.rewardPointCap * rewardScale).roundToInt()
		return SOLAR_SIEGE_REWARDS_BAG.generateRewards(points)
	}

	class RewardsBag private constructor(private val rewardCosts: List<RewardBag>) {
		/**
		 * Generates a rewards map when provided with points
		 * The map is item strings to quantity
		 **/
		fun generateRewards(points: Int): Map<String, Int> {
			val rewards = mutableMapOf<String, AtomicInteger>()

			var budget = points

			val trimmed = rewardCosts.toMutableList()

			while (budget > 0) {
				trimmed.removeAll { it.cost > budget }

				if (trimmed.isEmpty()) break

				val reward = trimmed.random()

				budget -= reward.cost
				rewards.getOrPut(reward.itemString) { AtomicInteger() }.addAndGet(reward.amount)
			}

			return rewards.mapValues { entry -> entry.value.get() }
		}

		companion object {
			fun builder(): Builder = Builder()
		}

		class Builder() {
			val rewardCosts = mutableListOf<RewardBag>()

			fun addReward(itemString: String, amount: Int, cost: Int): Builder {
				rewardCosts.add(RewardBag(itemString, amount, cost))
				return this
			}

			fun addReward(material: Material, amount: Int, cost: Int): Builder {
				rewardCosts.add(RewardBag(toItemString(material), amount, cost))
				return this
			}

			fun addReward(itemStack: ItemStack, amount: Int, cost: Int): Builder {
				rewardCosts.add(RewardBag(toItemString(itemStack), amount, cost))
				return this
			}

			fun addReward(customItem: IonRegistryKey<CustomItem, out CustomItem>, amount: Int, cost: Int): Builder {
				rewardCosts.add(RewardBag(customItem.key, amount, cost))
				return this
			}

			fun build(): RewardsBag {
				return RewardsBag(rewardCosts)
			}
		}

		data class RewardBag(val itemString: String, val amount: Int, val cost: Int)
	}
}
