package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.rewardAchievement
import net.starlegacy.feature.starship.event.StarshipDetectEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@BukkitListener
@Suppress("unused")
class DetectShipListener : Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onDetectShip(event: StarshipDetectEvent) {
		event.player.rewardAchievement(Achievement.DETECT_SHIP)
	}
}