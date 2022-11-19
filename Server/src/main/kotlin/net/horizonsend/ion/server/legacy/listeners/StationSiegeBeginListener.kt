package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.StationSiegeBeginEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class StationSiegeBeginListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onStationSiegeBeginEvent(event: StationSiegeBeginEvent) {
		event.player.rewardAchievement(Achievement.SIEGE_STATION)
	}
}