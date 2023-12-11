package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.achievements.Achievement
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.miscellaneous.utils.get
import org.bukkit.entity.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.pow
import kotlin.math.sqrt

class StandardRewardsProvider(private val starship: ActiveStarship) : RewardsProvider {
	override fun onSink() {
		val dataMap = starship.damagers
			.filter { (damager, _) ->
				if (damager !is PlayerDamager) return@filter false

				// require they be online to get xp
				// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
				return@filter !damager.player.hasPermission("starships.noxp")
			}

		processDamagers(this.starship, dataMap)
	}

	private fun processDamagers(
		starship: ActiveStarship,
		dataMap: Map<Damager, ShipKillXP.ShipDamageData> // Filtered
	) {
		val sum = dataMap.values.sumOf { it.points.get() }

		for ((damager, data) in dataMap.entries) {
			val (points, timeStamp) = data

			if (timeStamp < ShipKillXP.damagerExpiration) continue

			val player = (damager as? PlayerDamager)?.player ?: continue // shouldn't happen
			val killedSize = starship.initialBlockCount.toDouble()

			val pilotNation = SLPlayer[player].nation

			val killedPlayer: Player? = (starship.controller as? PlayerController)?.player
			val killedNation = killedPlayer?.let { SLPlayer[it].nation }

			if (pilotNation != null && killedNation != null) {
				if (RelationCache[pilotNation, killedNation].ordinal >= 5) continue
			}

			val percent = points.get() / sum
			val xp = ((sqrt(killedSize.pow(2.0) / sqrt(killedSize * 0.00005))) * percent).toInt()

			if (xp > 0) {
				damager.rewardXP(xp)
				log.info("Gave ${player.name} $xp XP for ship-killing ${starship.controller.pilotName.plainText()}")
			}

			if (points.get() > 0 && player.uniqueId != killedPlayer?.uniqueId) player.rewardAchievement(Achievement.KILL_SHIP)
		}
	}

	companion object {
		private val log: Logger = LoggerFactory.getLogger(StandardRewardsProvider::class.java)
	}
}

