package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.common.ServerType
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.features.nations.utils.ACTIVE_AFTER_TIME
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

object Power : IonServerComponent() {
	override fun onEnable() {
		// award power to online players every hour
		// 20 ticks * 60 minutes converted to seconds
		Tasks.syncRepeat(0L, 20L * TimeUnit.MINUTES.toSeconds(60)) {
			for (player in Bukkit.getOnlinePlayers()) {
				if (ConfigurationFiles.serverConfiguration().serverName != "survival") continue
				SLXP.addPowerAsync(player.uniqueId, 5)
			}

			Tasks.async {
				for (nation in NationCache.all()) {
					val dominionCount = getDominionTerritoryCount(nation)
					if (dominionCount == 0) {
						if (nation.siegeable == true) Nation.setSiegeable(nation.id, false)
						continue
					}

					val power = Nation.getTotalPower(nation.id, ACTIVE_AFTER_TIME)
					val powerCost = dominionTerritoryCost(nation)

					if (nation.siegeable == true && power >= powerCost) {
						// Recovered and no longer siegable
						Nation.setSiegeable(nation.id, false)
						Discord.sendEmbed(
							ConfigurationFiles.discordSettings().eventsChannel,
							Embed(
								title = "Nation No Longer Siegeable",
								description = "${nation.name}'s power has recovered and no new sieges can be initiated on their territories!",
							)
						)
						val headerLine = template(
							text("{0}'s power has recovered and no new sieges can be initiated on their territories!", YELLOW),
							formatNationName(nation.id)
						)
						Notify.allOnline(ofChildren(headerLine, newline(), newline()))

					} else if ((nation.siegeable == false || nation.siegeable == null) && power < powerCost) {
						// Dropped below threshold due to inactivity and now siegable
						Nation.setSiegeable(nation.id, true)
						Discord.sendEmbed(
							ConfigurationFiles.discordSettings().eventsChannel,
							Embed(
								title = "Nation Siegeable",
								description = "${nation.name}'s power has dropped too low, their dominion territories can now be sieged!",
							)
						)
						val headerLine = template(
							text("{0}'s power has dropped too low! Their dominion territories can now be sieged!", YELLOW),
							formatNationName(nation.id)
						)
						Notify.allOnline(ofChildren(headerLine, newline(), newline()))
					}
				}
			}
		}
	}

	fun getDominionTerritoryCount(nation: NationCache.NationData): Int{
		return Regions.getAllOf<RegionDominionTerritory>().count { it.nation == nation.id }
	}

	fun dominionTerritoryCost(count: Int): Int = 10 * (count * (count + 1) / 2)

	fun dominionTerritoryCost(nation: NationCache.NationData): Int {
		return dominionTerritoryCost(getDominionTerritoryCount(nation))
	}

	fun canAffordAnotherTerritory(nation: NationCache.NationData, currentPower: Int): Boolean {
		val nextCount = getDominionTerritoryCount(nation) + 1
		return currentPower >= dominionTerritoryCost(nextCount)
	}

	@EventHandler
	fun modifyPowerOnPlayerDeath(event: PlayerDeathEvent) {
		if (ConfigurationFiles.serverConfiguration().serverName != "survival") return
		if (!event.player.isConnected) {
			log.info("Player ${event.player.name} has died, but is not connected.")
			return
		}
		val victim = event.player
		val killer = event.entity.killer
			?: (event.entity.lastDamageCause as? EntityDamageByEntityEvent)?.damager as? Player
		if (killer == null) {
			log.info("Player ${victim.name} has been killed by an unknown entity, no power gained.")
			return // only player vs. player kills should modify power
		}
		if (!killer.isConnected) {
			log.info("Player ${killer.name} killed ${victim.name}, but is not connected.")
			return
		}
		val timeStamp = System.currentTimeMillis()

		PlayerCache[victim].lastDeathTimestamp?.let { if ((timeStamp - it) < TimeUnit.MINUTES.toMillis(5L)){
			killer.information("That player has already been killed in the last 5 minutes, no power gained.")
			return
		}}

		SLXP.addPowerAsync(victim.uniqueId, -5)
		SLXP.addPowerAsync(killer.uniqueId, 2)

		val victimNationId = PlayerCache[victim].nationOid

		if (victimNationId == null) {
			log.info("Player ${victim.name} has died, but is not part of a nation.")
			return
		}

		Tasks.async {
			val data = SLPlayer[victim]
			SLPlayer.updateById(data._id, setValue(SLPlayer::lastDeathTimestamp, timeStamp))

			recalculateNationPower(victimNationId)
		}
	}

	/**
	 * Recalculates the power of a nation and updates its siegeable status based on its power level
	 * in relation to its dominion territory cost. Sends notifications if the nation's territory
	 * becomes siegeable.
	 *
	 * Run this asynchronously to avoid blocking the main thread.
	 *
	 * @param nationId The unique identifier of the nation whose power is to be recalculated.
	 */
	fun recalculateNationPower(nationId: Oid<Nation>) {
		val power = Nation.getTotalPower(nationId, ACTIVE_AFTER_TIME)

		val powerCost = dominionTerritoryCost(NationCache[nationId])
		val dominionCount = getDominionTerritoryCount(NationCache[nationId])
		if (dominionCount == 0) return // Early return if no territories

		if (power < powerCost && (NationCache[nationId].siegeable == false || NationCache[nationId].siegeable == null)) {
			Nation.setSiegeable(nationId, true)
			val victimNationName = NationCache[nationId].name
			val victimNationNameFormatted = formatNationName(nationId)

			Discord.sendEmbed(
				ConfigurationFiles.discordSettings().eventsChannel,
				Embed(
					title = "Nation Siegeable",
					description = "$victimNationName's power has dropped too low, their territory can now be sieged!",
				)
			)
			val headerLine = template(
				text("{0}'s power has dropped too low! Their territory can now be sieged!", YELLOW),
				victimNationNameFormatted
			)

			val globalMessage = ofChildren(
				headerLine, newline(),
				newline(),
			)

			Notify.allOnline(globalMessage)
		}
	}
}
