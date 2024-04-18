package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AIItemRewardProvider(
	override val starship: ActiveControlledStarship,
	override val configuration: AITemplate.ItemRewardProviderConfiguration
) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)
	val items = configuration.items.associate { (string, count, percent) ->
		Bazaars.fromItemString(string).apply { amount = count } to percent
	}

	override fun processPrimaryDamagerRewards(damager: PlayerDamager, sum: Int, dataMap: ShipKillXP.ShipDamageData) {
		val items = items.filterValues { testRandom(it) }

		Tasks.sync {
			items.keys.forEach {
				Bazaars.dropItems(it, it.amount, damager.player)

				damager.sendMessage(template(
					message = Component.text("Received {0}x {1} for defeating {2}", NamedTextColor.GREEN),
					it.amount,
					it.displayName(),
					starship.getDisplayName()
				))
			}
		}
	}
}
