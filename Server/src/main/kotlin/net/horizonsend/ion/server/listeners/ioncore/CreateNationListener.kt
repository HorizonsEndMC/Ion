package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.CreateNationEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("unused")
class CreateNationListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCreateNation(event: CreateNationEvent) {
		event.player.rewardAchievement(Achievement.CREATE_NATION)
	}
}