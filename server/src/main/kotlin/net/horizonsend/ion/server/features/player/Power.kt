package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNation.Companion.getTotalPower
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.formatFrontierNationName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

object Power : IonServerComponent() {
	override fun onEnable() {
		// award power to online players every 30 minutes
		// 20 ticks * 30 minutes converted to seconds
		Tasks.syncRepeat(0L, 20L * TimeUnit.MINUTES.toSeconds(15)) {
			for (player in Bukkit.getOnlinePlayers()) {
				SLXP.addPowerAsync(player.uniqueId, 2)
			}

			Tasks.async {
				for (nation in FrontierNationCache.all()) {
					val power = getTotalPower(nation.id)
					if (nation.siegable && power >= 20) {
						FrontierNation.setSiegable(nation.id, false)
						Discord.sendEmbed(
							ConfigurationFiles.discordSettings().eventsChannel,
							Embed(
								title = "Frontier Nation No Longer Siegeable",
								description = "${nation.name}'s power has recovered and they can no longer be sieged!",
							)
						)
						val headerLine = template(
							text("{0}'s power has recovered and they can no longer be sieged!", YELLOW),
							formatFrontierNationName(nation.id)
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

		SLXP.addPowerAsync(victim.uniqueId, -4)
		SLXP.addPowerAsync(killer.uniqueId, 2)

		val victimNationId: Oid<FrontierNation> = PlayerCache[victim].frontierNationOid ?: return
		Tasks.async {
			val data = SLPlayer[victim]
			SLPlayer.updateById(data._id, setValue(SLPlayer::lastDeathTimestamp, timeStamp))
			val power = getTotalPower(victimNationId)
			if (power < 20 && !FrontierNationCache[victimNationId].siegable) {
				FrontierNation.setSiegable(victimNationId, true)
				val victimNationName = FrontierNationCache[victimNationId].name
				val victimNationNameFormatted = formatFrontierNationName(victimNationId)
				Discord.sendEmbed(
					ConfigurationFiles.discordSettings().eventsChannel,
					Embed(
						title = "Frontier Nation Siegeable",
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
