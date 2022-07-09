package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.IonServer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		if (event.material != Material.WARPED_FUNGUS_ON_A_STICK) return

		val item = event.item!! // If material is valid, then item is not null
		val itemMeta = item.itemMeta

		if (!itemMeta.hasCustomModelData()) return

		IonServer.customItems[itemMeta.customModelData]?.apply {
			when (event.action) {
				Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> onPrimaryInteract(event.player, item)
				Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> onSecondaryInteract(event.player, item)
				else -> return // Unknown Action Enum - We probably don't care, silently fail
			}
		}

		event.isCancelled = true
	}
}