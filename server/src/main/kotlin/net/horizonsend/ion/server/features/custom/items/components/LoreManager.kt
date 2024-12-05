package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

interface LoreManager {
	val priority: Int

	/**
	 * Returns the constructed lore lines of this block
	 **/
	fun getLines(customItem: NewCustomItem, itemStack: ItemStack): List<Component>

	/**
	 * Returns whether there should be an empty separator line between this lore block and the following, should there be one.
	 **/
	fun shouldIncludeSeparator(): Boolean
}
