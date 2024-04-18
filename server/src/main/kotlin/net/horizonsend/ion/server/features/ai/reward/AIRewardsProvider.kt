package net.horizonsend.ion.server.features.ai.reward

import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import org.slf4j.Logger
import java.util.concurrent.atomic.AtomicInteger

interface AIRewardsProvider : RewardsProvider {
	val configuration: AITemplate.AIRewardsProviderConfiguration
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

		dataMap.entries.maxByOrNull { it.value.points.get() }?.let {
			processPrimaryDamagerRewards(it.key)
		}

		for ((damager, data) in dataMap.entries) {
			val (points, _) = data
			val player = damager.player

			try {
				processDamagerRewards(damager, points, sum)
			} catch (e: Throwable) {
				log.error("Exception processing damager rewards: ${e.message}!")
				e.printStackTrace()
			}

			if (points.get() > 0) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}

	fun processDamagerRewards(damager: PlayerDamager, points: AtomicInteger, pointsSum: Int) {}
	fun processPrimaryDamagerRewards(damager: PlayerDamager) {}
}
