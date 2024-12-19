package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

interface LoreManager {
	val priority: Int

	/**
	 * Returns the constructed lore lines of this block
	 **/
	fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component>

	/**
	 * Returns whether there should be an empty separator line between this lore block and the following, should there be one.
	 **/
	fun shouldIncludeSeparator(): Boolean
}
