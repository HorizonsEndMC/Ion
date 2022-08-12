package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.StationCaptureEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class CaptureStationListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCaptureStation(event: StationCaptureEvent){
		event.player.rewardAchievement(Achievement.CAPTURE_STATION)
	}
}