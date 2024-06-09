package net.horizonsend.ion.server.features.custom.items.objects

import org.bukkit.inventory.ItemStack

interface LoreCustomItem {
	fun rebuildLore(itemStack: ItemStack)
}
