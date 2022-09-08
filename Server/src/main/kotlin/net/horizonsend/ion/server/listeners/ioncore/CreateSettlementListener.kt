package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.core.events.CreateSettlementEvent
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@BukkitListener
@Suppress("Unused")
class CreateSettlementListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCreateSettlementEvent(event: CreateSettlementEvent) {
		event.player.rewardAchievement(Achievement.CREATE_SETTLEMENT)
	}
}