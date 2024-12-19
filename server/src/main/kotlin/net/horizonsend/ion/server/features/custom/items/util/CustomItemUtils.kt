package net.horizonsend.ion.server.features.custom.items.util

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager.ComponentTypeData
import org.bukkit.inventory.ItemStack

fun <T : CustomItemComponent, Z : ComponentTypeData<T>, C : CustomItem> C.withComponent(type: CustomComponentTypes<T, Z>, data: T): C {
	customComponents.addComponent(type, data)
	return this
}

fun updateDurability(itemStack: ItemStack, power: Int, capacity: Int) {
	itemStack.setData(DataComponentTypes.MAX_DAMAGE, capacity)
	itemStack.setData(DataComponentTypes.DAMAGE, capacity - power)
}
