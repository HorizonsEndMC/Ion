package net.horizonsend.ion.proxy.listeners.waterfall

import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerVoteTime
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.IonProxy.Companion.Ion
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.time.LocalDateTime

class VotifierListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onVotifierEvent(event: VotifierEvent) {
		val playerData = PlayerData[event.vote.username] ?: return

		val siteEntry = Ion.configuration.voteSites.find { it.serviceName == event.vote.serviceName }
		val siteName = siteEntry?.serviceName ?: event.vote.serviceName

		playerData.voteTimes // in voteTimes for playerData
			.find { it.serviceName == event.vote.serviceName } // find voteTime with servicename
			?.update { dateTime = LocalDateTime.now() } // update the dateTime
			?: PlayerVoteTime.new { // create new if it doesnt exist
				player = playerData
				serviceName = event.vote.serviceName
				dateTime = LocalDateTime.now()
			}

		val thanksMessage = ComponentBuilder()
			.append(
				ComponentBuilder("Thanks to ${event.vote.username} for voting on ${siteEntry?.displayName ?: siteName}! Remember to vote for the server to help us grow the Horizon's End community!")
					.color(ChatColor.GOLD)
					.create()
			)

		Ion.proxy.broadcast(*thanksMessage.create())
	}
}
