package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.CompleteCargoRunEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class CompleteCargoRunListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCompleteCargoRun(event: CompleteCargoRunEvent) {
		event.player.rewardAchievement(Achievement.COMPLETE_CARGO_RUN)
	}
}