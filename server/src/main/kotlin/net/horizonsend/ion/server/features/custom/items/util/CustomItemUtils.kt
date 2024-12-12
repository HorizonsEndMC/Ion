package net.horizonsend.ion.server.features.custom.items.util

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager.ComponentTypeData
import org.bukkit.inventory.ItemStack

fun <T : CustomItemComponent, Z : ComponentTypeData<T>, C : NewCustomItem> C.withComponent(type: CustomComponentTypes<T, Z>, data: T): C {
	customComponents.addComponent(type, data)
	return this
}

fun updateDurability(itemStack: ItemStack, power: Int, capacity: Int) {
	itemStack.setData(DataComponentTypes.MAX_DAMAGE, capacity)
	itemStack.setData(DataComponentTypes.DAMAGE, capacity - power)
}
