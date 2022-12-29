package net.horizonsend.ion.server.listeners

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.PrepareItemEnchantEvent

class PrepareItemEnchantListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPrepareItemEnchantEvent(event: PrepareItemEnchantEvent) {
		event.offers[0] =
			if (Enchantment.SILK_TOUCH.canEnchantItem(event.item) || event.item.type == Material.BOOK)
				EnchantmentOffer(Enchantment.SILK_TOUCH, 1, 120)
			else null
		event.offers[1] = null
		event.offers[2] = null
	}
}