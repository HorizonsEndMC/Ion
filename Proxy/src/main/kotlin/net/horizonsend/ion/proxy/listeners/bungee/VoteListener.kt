package net.horizonsend.ion.proxy.listeners.bungee

import net.horizonsend.ion.proxy.ProxyConfiguration
import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

@Suppress("Unused")
class VoteListener(private val configuration: ProxyConfiguration) : Listener {
	@EventHandler
	fun onPlayerVote(event: VotifierEvent) {
		val site: String = event.vote.address
		val timestamp: Long = System.currentTimeMillis()

		if (configuration.voteSites.contains(event.vote.address)) {
			PlayerData[event.vote.username]?.update { voteTimes[site] = timestamp }
		}
	}
}