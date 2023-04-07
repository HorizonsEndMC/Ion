package net.horizonsend.ion.proxy.listeners.waterfall

import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.special
import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.time.LocalDateTime
import net.horizonsend.ion.common.database.PlayerVoteTime
import org.jetbrains.exposed.sql.transactions.transaction

class VotifierListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onVotifierEvent(event: VotifierEvent) = transaction {
		val playerData = PlayerData[event.vote.username] ?: return@transaction

		val siteEntry = PLUGIN.configuration.voteSites.find { it.serviceName == event.vote.serviceName }
		val siteName = siteEntry?.serviceName ?: event.vote.serviceName

		val voteTime = playerData.voteTimes
			.find { it.serviceName == event.vote.serviceName }
			?: PlayerVoteTime.new {
				player = playerData
				serviceName = event.vote.serviceName
			}

		voteTime.dateTime = LocalDateTime.now()

		PLUGIN.proxy.special("Thanks to ${event.vote.username} for voting on ${siteEntry?.displayName ?: siteName}! Remember to vote for the server to help us grow the Horizon's End community!")
	}
}
