package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.server.features.ai.reward.AIRewardCap.SHIP_SIZE_MULTIPLIER
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.cbrt

open class AICapRewardProvider(override val starship: ActiveStarship) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)

	override fun processDamagerRewards(
        damager: PlayerDamager,
        topDamagerPoints: AtomicInteger,
        points: AtomicInteger,
        pointsSum: Int,
        penalty: Double
    ) {

		val killedSize = starship.initialBlockCount.toDouble()
		val percent = points.get().toDouble() / pointsSum.toDouble()
		val score = (cbrt(killedSize) * SHIP_SIZE_MULTIPLIER * percent).toInt()

		if (score <= 0) return
		AIRewardCap.addToCap(damager.player, score)

		log.info("Gave $damager $score cap score for ship-killing AI vessel ${starship.identifier}")
	}
}
