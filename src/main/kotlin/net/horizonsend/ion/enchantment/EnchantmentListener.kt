package net.horizonsend.ion.enchantment

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment.SILK_TOUCH
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.inventory.PrepareAnvilEvent

internal class EnchantmentListener: Listener {
	@EventHandler
	fun onPrepareItemEnchantEvent(event: PrepareItemEnchantEvent) {
		// All of these suppressions are because Spigot don't know how nullability annotations work.
		@Suppress("UNCHECKED_CAST")
		val eventOffers = (event.offers as Array<EnchantmentOffer?>)

		eventOffers[0] = EnchantmentOffer(SILK_TOUCH, 120, 120)
		eventOffers[1] = null
		eventOffers[2] = null
	}

	@EventHandler
	fun onPrepareAnvilEvent(event: PrepareAnvilEvent) {
		if (event.inventory.firstItem == null) return
		if (event.inventory.secondItem == null) return

		if (event.inventory.secondItem!!.type == Material.ENCHANTED_BOOK) {
			if (!event.inventory.secondItem!!.enchantments.containsKey(SILK_TOUCH)) event.result = null
			else {
				event.result = event.inventory.firstItem!!.clone()
				event.result!!.enchantments[SILK_TOUCH] = 1
			}
		}
	}
}