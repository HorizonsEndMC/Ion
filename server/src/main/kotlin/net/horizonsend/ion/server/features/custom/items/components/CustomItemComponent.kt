package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import org.bukkit.inventory.ItemStack

interface CustomItemComponent {
	fun decorateBase(baseItem: ItemStack)

	fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute>
}
