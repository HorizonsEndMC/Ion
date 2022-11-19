package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.LevelUpEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class LevelUpListener: Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onLevelUpEvent(event: LevelUpEvent) {
		event.player.rewardAchievement(
			when (event.level) {
				10 -> Achievement.LEVEL_10
				20 -> Achievement.LEVEL_20
				40 -> Achievement.LEVEL_40
				80 -> Achievement.LEVEL_80
				else -> return
			}
		)
	}
}