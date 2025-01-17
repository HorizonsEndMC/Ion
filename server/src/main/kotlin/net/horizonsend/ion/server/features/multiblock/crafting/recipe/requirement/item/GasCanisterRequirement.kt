package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import org.bukkit.inventory.ItemStack

class GasCanisterRequirement(val gas: Gas, val amount: Int) : ItemRequirement {
	override fun matches(item: ItemStack?): Boolean {
		if (item == null) return false
		val customItem = item.customItem ?: return false
		if (customItem !is GasCanister) return false

		return customItem.gas == gas && customItem.getComponent(CustomComponentTypes.GAS_STORAGE).getFill(item) >= amount
	}

	override fun consume(item: ItemStack) {
		val customItem = item.customItem as GasCanister
		val component = customItem.getComponent(CustomComponentTypes.GAS_STORAGE)

		val currentFill = component.getFill(item)
		component.setFill(item, customItem, currentFill - amount)
	}
}
