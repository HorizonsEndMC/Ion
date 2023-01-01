package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.ShipKillEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class ShipKillListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onShipKillEvent(event: ShipKillEvent) {
		if (event.killer != event.player) event.player.rewardAchievement(Achievement.KILL_SHIP)
	}
}
