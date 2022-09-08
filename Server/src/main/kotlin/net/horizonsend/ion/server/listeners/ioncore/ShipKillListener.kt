package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.ShipKillEvent
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@BukkitListener
@Suppress("Unused")
class ShipKillListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onShipKillEvent(event: ShipKillEvent) {
		event.player.rewardAchievement(Achievement.KILL_SHIP)
	}
}