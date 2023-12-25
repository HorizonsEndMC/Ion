package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
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
			log.info("Gave $damager $xp XP and ${template.creditReward} credits for ship-killing AI vessel ${starship.identifier}")
		}
	}
}
