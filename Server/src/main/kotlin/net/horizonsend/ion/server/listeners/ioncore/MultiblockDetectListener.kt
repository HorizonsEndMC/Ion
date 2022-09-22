package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.MultiblockDetectEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class MultiblockDetectListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onMultiblockDetectEvent(event: MultiblockDetectEvent) {
		event.player.rewardAchievement(Achievement.DETECT_MULTIBLOCK)
	}
}