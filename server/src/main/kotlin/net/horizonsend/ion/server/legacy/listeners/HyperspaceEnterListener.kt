package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.HyperspaceEnterEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class HyperspaceEnterListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onHyperspaceEnterEvent(event: HyperspaceEnterEvent) {
		event.player.rewardAchievement(Achievement.USE_HYPERSPACE)
	}
}
