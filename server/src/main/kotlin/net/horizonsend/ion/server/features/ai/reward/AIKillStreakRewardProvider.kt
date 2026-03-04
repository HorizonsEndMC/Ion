package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.cbrt

class AIKillStreakRewardProvider(override val starship: ActiveStarship, val configuration: AITemplate.KillStreakRewardProviderConfiguration) : AIRewardsProvider {
	override val log: Logger = LoggerFactory.getLogger(javaClass)

	override fun processDamagerRewards(
		damager: PlayerDamager,
		topDamagerPoints: AtomicInteger,
		points: AtomicInteger,
		pointsSum: Int,
		penalty: Double
	) {
		val killedSize = starship.initialBlockCount.toDouble()
		val damagerSize = (damager.starship?.initialBlockCount ?: 2000).toDouble()
		val ratio = (cbrt(killedSize) / cbrt(damagerSize)).coerceAtMost(3.0)
		val difficultyMultiplier = (starship.controller as? AIController)?.getCoreModuleByType<DifficultyModule>()?.rewardMultiplier ?: 1.0
		val topPercent = topDamagerPoints.get().toDouble() / pointsSum.toDouble()
		val percent = points.get().toDouble() / pointsSum.toDouble()
		val score = (ratio * (percent / topPercent) * configuration.streakMultiplier * difficultyMultiplier * penalty).toInt()

		if (score <= 0) return

		AIKillStreak.rewardHeat(damager.player, score)

		log.info("Gave $damager $score heat score for ship-killing AI vessel ${starship.identifier}")
	}
}
