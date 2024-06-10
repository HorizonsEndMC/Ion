package net.horizonsend.ion.server.features.custom.items.objects

import org.bukkit.inventory.ItemStack

/**
 * For custom items that may need to juggle multiple lore entries.
 * Individual interfaces may use this to cause others to be regenerated upon changes
 **/
interface LoreCustomItem {
	fun rebuildLore(itemStack: ItemStack)
}
