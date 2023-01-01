package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.events.CreateSettlementEvent
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class CreateSettlementListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCreateSettlementEvent(event: CreateSettlementEvent) {
		event.player.rewardAchievement(Achievement.CREATE_SETTLEMENT)
	}
}
