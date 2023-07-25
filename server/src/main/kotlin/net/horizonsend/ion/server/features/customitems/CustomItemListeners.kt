package net.horizonsend.ion.server.features.customitems

import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.meta.Damageable

class CustomItemListeners : SLEventListener() {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun rightClick(event: PlayerInteractEvent) {
		if (event.item == null) return

		val customItem = event.item?.customItem ?: return
		when (event.action) {
			Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
				customItem.handleSecondaryInteract(event.player, event.player.inventory.itemInMainHand)
				event.isCancelled = true
			}

			else -> return // Unknown Action Enum - We probably don't care, silently fail
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onPlayerItemDamageEvent(event: PlayerItemDamageEvent) {
		if (event.item.customItem == null) return
		val damageable = event.item.itemMeta as? Damageable ?: return // for potential durability manipulation in the future
		event.isCancelled = true
	}

	@EventHandler
	@Suppress("Unused")
	fun onEntityShootBow(event: EntityShootBowEvent) {
		val entity = event.entity as? LivingEntity ?: return
		val offhand = entity.equipment?.itemInMainHand ?: return

		val customItem = offhand.customItem ?: return

		customItem.handleSecondaryInteract(entity, offhand)
		event.isCancelled = true
	}

	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("unused")
	fun onPlayerSwapItem(event: PlayerSwapHandItemsEvent) {
		// We have to get it from the inventory and not the event, otherwise things break
		val itemStack = event.player.inventory.itemInMainHand
		val customItem = itemStack.customItem ?: return

		event.isCancelled = true
		customItem.handleTertiaryInteract(event.player, itemStack)
	}
}
