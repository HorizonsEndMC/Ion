package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.core.events.LevelUpEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class LevelUpListener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onLevelUp(event: LevelUpEvent) {
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
