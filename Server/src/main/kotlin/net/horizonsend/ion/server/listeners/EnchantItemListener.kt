package net.horizonsend.ion.server.listeners

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.inventory.ItemStack

class EnchantItemListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onEnchantItemEvent(event: EnchantItemEvent) {
		event.isCancelled = true

		event.enchanter.level -= 120
		event.inventory.getItem(1)?.amount = event.inventory.getItem(1)?.amount!! - 1

		if (event.item.type == Material.BOOK) {
			event.inventory.removeItem(event.item)

			val book = ItemStack(Material.ENCHANTED_BOOK)
			book.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)

			event.inventory.addItem(book)
		} else {
			event.item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)
		}
	}
}