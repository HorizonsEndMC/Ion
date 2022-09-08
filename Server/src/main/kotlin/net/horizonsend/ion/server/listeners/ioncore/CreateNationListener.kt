package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.CreateNationEvent
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@BukkitListener
@Suppress("Unused")
class CreateNationListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCreateNationEvent(event: CreateNationEvent) {
		event.player.rewardAchievement(Achievement.CREATE_NATION)
	}
}