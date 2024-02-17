package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.miscellaneous.utils.mapNotNullTo
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow
import kotlin.math.sqrt

class AIRewardsProvider(val starship: ActiveStarship, val template: AISpawningConfiguration.AIStarshipTemplate) : RewardsProvider {
	override fun triggerReward() {
		val map = mutableMapOf<PlayerDamager, ShipKillXP.ShipDamageData>()

		starship.damagers.mapNotNullTo(map) filter@{ (damager, data) ->
			if (damager !is PlayerDamager) return@filter null
			if (data.lastDamaged < ShipKillXP.damagerExpiration) return@filter null

			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			if (damager.player.hasPermission("starships.noxp")) return@filter null

			damager to data
		}

		processDamagers(map)
	}

	private fun processDamagers(dataMap: Map<PlayerDamager, ShipKillXP.ShipDamageData>) {
		val sum = dataMap.values.sumOf { it.points.get() }

		for ((damager, data) in dataMap.entries) {
			val (points, _) = data
			val player = (damager as? PlayerDamager)?.player ?: continue // shouldn't happen

			processDamagerRewards(damager, points, sum)

			if (points.get() > 0) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}

	private fun processDamagerRewards(damager: PlayerDamager, points: AtomicInteger, pointsSum: Int) {
		val killedSize = starship.initialBlockCount.toDouble()

		val percent = points.get() / pointsSum
		val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent * template.xpMultiplier).toInt()
		val money = template.creditReward * percent

		if (xp > 0 || money > 0.0) {
			damager.rewardXP(xp)
			damager.rewardMoney(money)

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

	companion object {
		@JvmStatic
		private val log: Logger = LoggerFactory.getLogger(StandardRewardsProvider::class.java)
	}
}
