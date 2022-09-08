package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.BuySpawnShuttleEvent
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@BukkitListener
@Suppress("Unused")
class BuySpawnShuttleListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onBuySpawnShuttleEvent(event: BuySpawnShuttleEvent) {
		event.player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
	}
}