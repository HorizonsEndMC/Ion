package net.horizonsend.ion.server.features.custom.items.mods.tool.drops

import org.bukkit.inventory.ItemStack

interface DropModifier {
	val priority: Int

	fun modify(itemStack: ItemStack)
}
