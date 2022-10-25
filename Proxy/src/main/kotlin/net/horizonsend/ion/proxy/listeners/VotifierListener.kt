package net.horizonsend.ion.proxy.listeners

import com.vexsoftware.votifier.bungee.events.VotifierEvent
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.IonProxy.Companion.Ion
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class VotifierListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onVotifierEvent(event: VotifierEvent) {
		val site: String = event.vote.address

		if (Ion.configuration.voteSites.contains(site)) {
			PlayerData[event.vote.username]?.update {
				voteTimes[site] = System.currentTimeMillis()
			}
		}
	}
}