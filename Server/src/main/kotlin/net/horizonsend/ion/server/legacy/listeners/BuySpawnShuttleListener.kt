package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.BuySpawnShuttleEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class BuySpawnShuttleListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onBuySpawnShuttleEvent(event: BuySpawnShuttleEvent) {
		event.player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
	}
}