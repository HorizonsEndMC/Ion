package net.horizonsend.ion.server.features.custom.items.mods.drops

import org.bukkit.inventory.ItemStack

interface DropModifier {
	val priority: Int

	fun modify(itemStack: ItemStack)
}
