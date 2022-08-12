package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.StationSiegeBeginEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class StationSiegeListener : Listener{
	@EventHandler(priority = EventPriority.LOWEST)
	fun onStationSiege(event: StationSiegeBeginEvent){
		event.player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
	}
}