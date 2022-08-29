package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent

@Suppress("Unused")
class EnchantItemListener : Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	fun onEnchantItemEvent(event: EnchantItemEvent) {
		event.isCancelled = true

		event.enchanter.level -= 120
		event.inventory.getItem(1)?.amount = event.inventory.getItem(1)?.amount!! - 1

			event.item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)
	}
}