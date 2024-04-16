package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

class AIXPRewardProvider(override val starship: ActiveStarship, override val configuration: AITemplate.SLXPRewardProviderConfiguration) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)

	override fun processDamagerRewards(damager: PlayerDamager, points: AtomicInteger, pointsSum: Int) {
		val killedSize = starship.initialBlockCount.toDouble()

		val percent = points.get().toDouble() / pointsSum.toDouble()
		val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent * configuration.xpMultiplier).toInt()

		if (xp <= 0) return

		damager.rewardXP(xp)

		damager.sendMessage(template(
			message = text("Received {0} XP for defeating {1}", NamedTextColor.DARK_PURPLE),
			text(xp, NamedTextColor.LIGHT_PURPLE),
			starship.getDisplayName()
		))

		log.info("Gave $damager $xp XP for ship-killing AI vessel ${starship.identifier}")
	}
}
