package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.HyperspaceEnterEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class HyperspaceEnterListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onHyperSpaceEnter(event: HyperspaceEnterEvent) {
		event.player.rewardAchievement(Achievement.USE_HYPERSPACE)
	}
}