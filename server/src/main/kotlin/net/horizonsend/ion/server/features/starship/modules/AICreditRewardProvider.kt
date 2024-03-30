package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

open class AICreditRewardProvider(override val starship: ActiveStarship, override val configuration: AISpawningConfiguration.AIStarshipTemplate.CreditRewardProviderConfiguration) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)

	override fun processDamagerRewards(damager: PlayerDamager, points: AtomicInteger, pointsSum: Int) {
		val percent = points.get().toDouble() / pointsSum.toDouble()
		val money = configuration.creditReward * percent

		if (money <= 0.0) return

		damager.rewardMoney(money)

		damager.sendMessage(template(
			message = text("Received {0} for defeating {1}", NamedTextColor.YELLOW),
			configuration.creditReward.toCreditComponent(),
			starship.getDisplayName()
		))

		log.info("Gave $damager ${configuration.creditReward} credits for ship-killing AI vessel ${starship.identifier}")
	}
}
