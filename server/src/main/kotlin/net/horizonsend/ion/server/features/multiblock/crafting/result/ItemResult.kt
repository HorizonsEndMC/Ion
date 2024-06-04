package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import org.bukkit.block.Sign
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

open class ItemResult(private val result: ItemStack, private val count: Int = 1) : MultiblockRecipeResult {
	constructor(item: CustomItem, count: Int = 1) : this(item.constructItemStack(), count)

	override fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean {
		if (((craftingInventory as? CraftingInventory)?.result?.amount ?: 0) >= result.maxStackSize) return false
		if (((craftingInventory as? FurnaceInventory)?.result?.amount ?: 0) >= result.maxStackSize) return false
		return true
	}

	override fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign) {
		applyResultTo(craftingInventory) { result.asQuantity(count) }
	}

	private fun applyResultTo(inventory: Inventory, result: Supplier<ItemStack>) {
		when (inventory) {
			is CraftingInventory -> inventory.result?.add(1) ?: run { inventory.result = result.get() }
			is FurnaceInventory -> inventory.result?.add(1) ?: run { inventory.result = result.get() }
		}
	}
}
