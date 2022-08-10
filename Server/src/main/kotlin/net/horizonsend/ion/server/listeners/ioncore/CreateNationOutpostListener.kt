package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.CreateNationOutpostEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("unused")
class CreateNationOutpostListener : Listener{
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCreateNationOutpost(event:CreateNationOutpostEvent){
		event.player.rewardAchievement(Achievement.CREATE_OUTPOST)
	}
}