package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.ionCore
import net.horizonsend.ion.server.utilities.rewardAchievement
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent

@BukkitListener
@Suppress("Unused")
class PlayerPickUpItemListener : Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerAttemptPickupItemEvent(event: PlayerAttemptPickupItemEvent) {
		ionCore {
			event.player.rewardAchievement(
				when (event.item.itemStack) {
					CustomItems.MINERAL_TITANIUM.singleItem() -> Achievement.ACQUIRE_TITANIUM
					CustomItems.MINERAL_ALUMINUM.singleItem() -> Achievement.ACQUIRE_ALUMINIUM
					CustomItems.MINERAL_CHETHERITE.singleItem() -> Achievement.ACQUIRE_CHETHERITE
					CustomItems.MINERAL_URANIUM.singleItem() -> Achievement.ACQUIRE_URANIUM
					else -> return
				}
			)
		}
	}
}