package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.CompleteCargoRunEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class CompleteCargoRunListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCompleteCargoRunEvent(event: CompleteCargoRunEvent) {
		event.player.rewardAchievement(Achievement.COMPLETE_CARGO_RUN)
	}
}