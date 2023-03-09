package net.horizonsend.ion.proxy.listeners.waterfall

import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerVoteTime
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.common.extensions.special
import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.time.LocalDateTime

class VotifierListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onVotifierEvent(event: VotifierEvent) {
		val playerData = PlayerData[event.vote.username] ?: return

		val siteEntry = PLUGIN.configuration.voteSites.find { it.serviceName == event.vote.serviceName }
		val siteName = siteEntry?.serviceName ?: event.vote.serviceName

		playerData.voteTimes // in voteTimes for playerData
			.find { it.serviceName == event.vote.serviceName } // find voteTime with servicename
			?.update { dateTime = LocalDateTime.now() } // update the dateTime
			?: PlayerVoteTime.new { // create new if it doesnt exist
				player = playerData
				serviceName = event.vote.serviceName
				dateTime = LocalDateTime.now()
			}

		PLUGIN.proxy.special("Thanks to ${event.vote.username} for voting on ${siteEntry?.displayName ?: siteName}! Remember to vote for the server to help us grow the Horizon's End community!")
	}
}
