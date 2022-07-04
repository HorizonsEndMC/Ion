package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class PrepareAnvilListener : Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("Unused")
	fun onPrepareAnvilEvent(event: PrepareAnvilEvent) {
		if (event.inventory.firstItem == null) return
		if (event.inventory.secondItem == null) return

		if (event.inventory.secondItem!!.type != Material.ENCHANTED_BOOK) return

		if (!event.inventory.secondItem!!.enchantments.containsKey(Enchantment.SILK_TOUCH)) {
			event.result = null
		} else {
			event.result = event.inventory.firstItem!!.clone()
			event.result!!.enchantments[Enchantment.SILK_TOUCH] = 1
		}
	}
}