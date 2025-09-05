package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

open class AICreditRewardProvider(override val starship: ActiveStarship, val configuration: AITemplate.CreditRewardProviderConfiguration) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)

	override fun processDamagerRewards(
		damager: PlayerDamager,
		topDamagerPoints: AtomicInteger,
		points: AtomicInteger,
		pointsSum: Int
	) {
		val difficultyMultiplier = (starship.controller as? AIController)?.getCoreModuleByType<DifficultyModule>()?.rewardMultiplier ?: 1.0
		val topPercent = topDamagerPoints.get().toDouble() / pointsSum.toDouble()
		debugAudience.debug("topPercent: $topPercent")
		val killStreakBonus = AIKillStreak.getHeatMultiplier(damager.player)
		debugAudience.debug("killStreakBonus: $killStreakBonus")
		val percent = points.get().toDouble() / pointsSum.toDouble()
		debugAudience.debug("percent: $percent")
		val money = configuration.creditReward * percent / topPercent * difficultyMultiplier * killStreakBonus

		if (money <= 0.0) return

		damager.rewardMoney(money)

		damager.sendMessage(
			template(
				message = text("Received {0} for defeating {1}", NamedTextColor.YELLOW),
				money.roundToHundredth().toCreditComponent(),
				starship.getDisplayName()
			)
		)

		log.info("Gave $damager ${configuration.creditReward} credits for ship-killing AI vessel ${starship.identifier}")
	}
}
