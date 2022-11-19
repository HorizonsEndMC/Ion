package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import net.starlegacy.feature.starship.event.StarshipDetectEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("unused")
class DetectShipListener : Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onDetectShip(event: StarshipDetectEvent) {
		event.player.rewardAchievement(Achievement.DETECT_SHIP)
	}
}