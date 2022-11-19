package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.MultiblockDetectEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
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