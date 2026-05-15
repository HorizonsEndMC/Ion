package net.horizonsend.ion.server.features.player

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
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

object Power : IonServerComponent() {
	override fun onEnable() {
		// award power to online players every hour
		// 20 ticks * 60 minutes converted to seconds
		Tasks.syncRepeat(0L, 20L * TimeUnit.MINUTES.toSeconds(60)) {
			for (player in Bukkit.getOnlinePlayers()) {
				SLXP.addPowerAsync(player.uniqueId, 5)
			}

			Tasks.async {
				for (nation in NationCache.all()) {
					val dominionCount = getDominionTerritoryCount(nation)
					if (dominionCount == 0) {
						// ensure they're never siegable, silently
						if (nation.siegable) Nation.setSiegable(nation.id, false)
						continue
					}
					val power = Nation.getTotalPower(nation.id, ACTIVE_AFTER_TIME)
					val powerCost = dominionTerritoryCost(nation)
					if (nation.siegable && power >= powerCost) {
						Nation.setSiegable(nation.id, false)
						Discord.sendEmbed(
							ConfigurationFiles.discordSettings().eventsChannel,
							Embed(
								title = "Nation No Longer Siegeable",
								description = "${nation.name}'s power has recovered and they can no longer be sieged!",
							)
						)
						val headerLine = template(
							text("{0}'s power has recovered and they can no longer be sieged!", YELLOW),
							formatNationName(nation.id)
						)

						val globalMessage = ofChildren(
							headerLine, newline(),
							newline(),
						)
						Notify.allOnline(globalMessage)
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
		if (!event.player.isConnected) return
		val victim = event.player
		val killer = event.entity.killer ?: return // only player vs. player kills should modify power
		if (!event.entity.killer!!.isConnected) return
		val timeStamp = System.currentTimeMillis()

		PlayerCache[victim].lastDeathTimestamp?.let { if ((timeStamp - it) < TimeUnit.MINUTES.toMillis(5L)){
			killer.information("That player has already been killed in the last 5 minutes, no power gained.")
			return
		}}

		SLXP.addPowerAsync(victim.uniqueId, -5)
		SLXP.addPowerAsync(killer.uniqueId, 2)

		val victimNationId: Oid<Nation> = PlayerCache[victim].nationOid ?: return
		Tasks.async {
			val data = SLPlayer[victim]
			SLPlayer.updateById(data._id, setValue(SLPlayer::lastDeathTimestamp, timeStamp))
			val power = Nation.getTotalPower(victimNationId, ACTIVE_AFTER_TIME)
			val powerCost = dominionTerritoryCost(NationCache[victimNationId])
			val dominionCount = getDominionTerritoryCount(NationCache[victimNationId])
			if (dominionCount == 0) return@async // Early return if no territories
			if (power < powerCost && !NationCache[victimNationId].siegable) {
				Nation.setSiegable(victimNationId, true)
				val victimNationName = NationCache[victimNationId].name
				val victimNationNameFormatted = formatNationName(victimNationId)
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
}
