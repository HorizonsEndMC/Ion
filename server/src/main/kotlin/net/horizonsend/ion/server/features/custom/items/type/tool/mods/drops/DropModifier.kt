package net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops

import org.bukkit.inventory.ItemStack

interface DropModifier {
	val priority: Int

	fun modifyDrop(itemStack: ItemStack): Boolean
}
