package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import org.bukkit.inventory.ItemStack

interface CustomItemComponent {
	fun decorateBase(baseItem: ItemStack, customItem: CustomItem)

	fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute>

	fun registerSerializers(serializationManager: SerializationManager) {}
}
