package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

class TickRecievierModule(val interval: Int, private val tickReciever: (LivingEntity, ItemStack, CustomItem) -> Unit) : CustomItemComponent {
	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {}
	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf()

	fun handleTick(user: LivingEntity, item: ItemStack, customItem: CustomItem) {
		tickReciever.invoke(user, item, customItem)
	}
}
