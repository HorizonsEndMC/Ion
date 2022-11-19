package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.CreateNationOutpostEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class CreateNationOutpostListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCreateNationOutpostEvent(event: CreateNationOutpostEvent) {
		event.player.rewardAchievement(Achievement.CREATE_OUTPOST)
	}
}