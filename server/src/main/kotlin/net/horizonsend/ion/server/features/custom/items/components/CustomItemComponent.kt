package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import org.bukkit.inventory.ItemStack

interface CustomItemComponent {
	fun decorateBase(baseItem: ItemStack, customItem: NewCustomItem)

	fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute>
}
