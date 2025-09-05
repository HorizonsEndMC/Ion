package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.miscellaneous.randomFloat
import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.cbrt

class AIItemBagRewardProvider(
	override val starship: ActiveControlledStarship,
	val configuration: AITemplate.ItemBagRewardProviderConfiguration
) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)
	val items = configuration.items.associate { (string, count, weight) ->
		fromItemString(string).apply { amount = count.get() } to weight
	}

	override fun processDamagerRewards(
		damager: PlayerDamager,
		topDamagerPoints: AtomicInteger,
		points: AtomicInteger,
		pointsSum: Int) {
		val difficultyMultiplier = (starship.controller as? AIController)?.getCoreModuleByType<DifficultyModule>()?.rewardMultiplier ?: 1.0
		val topPercent = topDamagerPoints.get().toDouble() / pointsSum.toDouble()
		val killStreakBonus = AIKillStreak.getHeatMultiplier(damager.player)
		val percent = points.get().toDouble() / pointsSum.toDouble()
		val score = ((percent / topPercent) * difficultyMultiplier * killStreakBonus)
		if (score <= 0) return

		val maxBagSize = configuration.maxBagSize * score
		val minBagSize = maxBagSize * configuration.minBagModifier
		val budget = randomDouble(minBagSize,maxBagSize)

		val items = getItems(budget)

		Tasks.sync {
			items.forEach {
				Bazaars.giveOrDropItems(it, it.amount, damager.player)

				damager.sendMessage(
					template(
						message = Component.text("Received {0}x {1} for defeating {2}", NamedTextColor.GREEN),
						it.amount,
						it.displayName(),
						starship.getDisplayName()
					)
				)
			}
		}
	}

	private fun getItems(budget : Double): Set<ItemStack> {
		var points = budget
		val bag = mutableSetOf<ItemStack>()//TODO: Combine items into one itemstack

		while (points > 0) {
			val remainingAvailable = items.filter { it.value <= points }
			if (remainingAvailable.isEmpty()) break

			val item = remainingAvailable.toList().random()

			points -= item.second
			bag += item.first
		}

		return bag
	}
}
