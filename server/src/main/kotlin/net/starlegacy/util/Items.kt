package net.starlegacy.util

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/** Since empty items can either be null or air, extra work is needed to see if it's empty */
fun isEmpty(itemStack: ItemStack?): Boolean = itemStack == null || itemStack.type == Material.AIR

fun ItemStack.updateMeta(block: (ItemMeta) -> Unit): ItemStack = apply {
	itemMeta = requireNotNull(itemMeta) { "No item meta for $type!" }.apply(block)
}

fun ItemStack.setDisplayNameAndGet(name: String): ItemStack = updateMeta { it.setDisplayName(name) }

fun ItemStack.setDisplayNameAndGet(name: Component): ItemStack = updateMeta { it.displayName(name) }

val ItemStack.displayName
	get() =
		if (this.hasItemMeta() && this.itemMeta.hasDisplayName()) {
			this.itemMeta.displayName
		} else {
			this.i18NDisplayName ?: this.type.name
		}

fun ItemStack.setLoreAndGet(lines: List<String>): ItemStack = apply { this.lore = lines }
