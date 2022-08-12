package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.ShipKillEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class ShipKillListener : Listener{
	@EventHandler(priority = EventPriority.LOWEST)
	fun onShipKill(event: ShipKillEvent){
		event.player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
	}
}