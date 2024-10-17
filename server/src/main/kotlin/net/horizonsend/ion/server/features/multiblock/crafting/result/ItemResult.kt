package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ItemIngredient.Companion.MAIN_FURNACE_STRING
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class ItemResult<T: MultiblockEntity>(
	private val result: ItemStack,
	private val count: Int,
	val outputInventory: String,
	val check: (Inventory, ItemStack) -> Boolean,
	val add: (Inventory, ItemStack) -> Unit,
) : MultiblockRecipeResult<T> {

	override fun canFit(context: RecipeExecutionContext<T>): Boolean {
		val inv = context.getLabeledInventory(outputInventory)
		return check.invoke(inv, result.asQuantity(count))
	}

	override fun execute(context: RecipeExecutionContext<T>) {
		val inv = context.getLabeledInventory(outputInventory)
		add.invoke(inv, result.asQuantity(count))
	}

	companion object {
		fun <T : MultiblockEntity> furnaceRecipeResult(resultItem: ItemStack, quantity: Int = 1) = ItemResult<T>(
			resultItem,
			quantity,
			MAIN_FURNACE_STRING,
			check@ { inv, item ->
				val resultSLot = (inv as FurnaceInventory).result ?: return@check true
				if (!resultSLot.isSimilar(item)) return@check true
				resultSLot.amount + quantity < resultItem.maxStackSize
			},
			add@ { inv, item ->
				val resultSLot = (inv as FurnaceInventory).result
				if (resultSLot?.isSimilar(item) == true) {
					resultSLot.amount += quantity
				}

				if (resultSLot == null) inv.result = item
			}
		)

		fun <T : MultiblockEntity> furnaceRecipeResult(resultItem: CustomItem, quantity: Int = 1) = furnaceRecipeResult<T>(resultItem.constructItemStack(), quantity)
	}
}
