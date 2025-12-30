package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.slf4j.Logger
import java.util.concurrent.atomic.AtomicInteger

interface AIRewardsProvider : RewardsProvider {
	val starship: ActiveStarship
	val log: Logger

	override fun triggerReward() {
		val map = starship.damagers.mapNotNull filter@{ (damager, data) ->
			if (damager !is PlayerDamager) return@filter null
			if (data.lastDamaged < ShipKillXP.damagerExpiration) return@filter null

			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			if (damager.player.hasPermission("starships.noxp")) return@filter null

			damager to data
		}.toMap()

		processDamagers(map)
	}

	private fun processDamagers(dataMap: Map<PlayerDamager, ShipKillXP.ShipDamageData>) {
		val sum = dataMap.values.sumOf { it.points.get() }
		debugAudience.debug("damager sum: $sum")

		val topDamager = dataMap.entries.maxByOrNull { it.value.points.get() }

		topDamager?.let {
			processPrimaryDamagerRewards(it.key, sum, it.value)
		}
		if (topDamager != null) {
			debugAudience.debug("top damager points: ${topDamager.value.points.get()}")
		}

		for ((damager, data) in dataMap.entries) {
			val points = data.points
			val player = damager.player
			debugAudience.debug("points: ${points.get()}, player : ${player.name}")

			try {
				AIRewardCap.processDamager(starship,damager,points,sum)
				val penalty = AIRewardCap.getPenalty(damager.player)
				if (penalty == 0.0) continue
				processDamagerRewards(damager, topDamager!!.value.points, points, sum, penalty)
			} catch (e: Throwable) {
				log.error("Exception processing damager rewards: ${e.message}!")
				e.printStackTrace()
			}

			if (points.get() > 0) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}

	fun processDamagerRewards(
		damager: PlayerDamager,
		topDamagerPoints: AtomicInteger,
		points: AtomicInteger,
		pointsSum: Int,
		penalty: Double
	) {
	}

	fun processPrimaryDamagerRewards(damager: PlayerDamager, sum: Int, dataMap: ShipKillXP.ShipDamageData) {}
}
