package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.StationCaptureEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class CaptureStationListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onStationCaptureEvent(event: StationCaptureEvent) {
		event.player.rewardAchievement(Achievement.CAPTURE_STATION)
	}
}
