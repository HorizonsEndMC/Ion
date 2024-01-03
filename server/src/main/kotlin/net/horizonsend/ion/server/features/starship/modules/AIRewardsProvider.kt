package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

class AIRewardsProvider(starship: ActiveStarship, val template: AISpawningConfiguration.AIStarshipTemplate) : StandardRewardsProvider(starship) {
	override fun processDamagerRewards(damager: Damager, points: AtomicInteger, lastDamaged: Long, pointsSum: Int) {
		val killedSize = starship.initialBlockCount.toDouble()

		val percent = points.get() / pointsSum
		val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent * template.xpMultiplier).toInt()

		if (xp > 0) {
			damager.rewardXP(xp)
			damager.rewardMoney(template.creditReward)

			damager.sendMessage(template(
				message = text("Received {0} XP for defeating {1}", NamedTextColor.DARK_PURPLE),
				text(xp, NamedTextColor.LIGHT_PURPLE),
				miniMessage().deserialize(template.miniMessageName),
			))

			damager.sendMessage(template(
				message = text("Received {0} for defeating {1}", NamedTextColor.YELLOW),
				template.creditReward.toCreditComponent(),
				miniMessage().deserialize(template.miniMessageName),
			))

			log.info("Gave $damager $xp XP and ${template.creditReward} credits for ship-killing AI vessel ${starship.identifier}")
		}
	}
}
