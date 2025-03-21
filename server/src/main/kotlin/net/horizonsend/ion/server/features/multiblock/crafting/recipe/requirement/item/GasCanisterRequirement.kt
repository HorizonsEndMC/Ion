package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item

import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.crafting.input.GasFurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import org.bukkit.inventory.ItemStack

class GasCanisterRequirement(val gas: Gas, val amount: Int) : ItemRequirement {
	override fun matches(item: ItemStack?): Boolean {
		if (item == null) return false
		val customItem = item.customItem ?: return false
		if (customItem !is GasCanister) return false

		return customItem.gas == gas && customItem.getComponent(CustomComponentTypes.GAS_STORAGE).getFill(item) >= amount
	}

	override fun consume(item: ItemStack, environment: RecipeEnviornment) {
		val customItem = item.customItem as GasCanister
		val component = customItem.getComponent(CustomComponentTypes.GAS_STORAGE)

		val currentFill = component.getFill(item)
		val newFill = currentFill - amount

		if (newFill > 0) {
			component.setFill(item, customItem, newFill)
		}
		else if (environment is GasFurnaceEnviornment) {
			item.amount = 0
			environment.discardInventory.addItem(GAS_CANISTER_EMPTY.getValue().constructItemStack())
		}
	}
}
