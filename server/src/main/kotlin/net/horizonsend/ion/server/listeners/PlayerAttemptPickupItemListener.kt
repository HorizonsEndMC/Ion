package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent

class PlayerAttemptPickupItemListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerAttemptPickupItemEvent(event: PlayerAttemptPickupItemEvent) {
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
