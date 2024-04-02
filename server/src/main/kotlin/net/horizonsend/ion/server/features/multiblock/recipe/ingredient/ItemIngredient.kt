package net.horizonsend.ion.server.features.multiblock.recipe.ingredient

import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class ItemIngredient(val ingredient: ItemStack, val amount: Int) : MultiblockRecipeIngredient {
	constructor(ingredient: CustomItem, amount: Int) : this(ingredient.constructItemStack(), amount)

	override fun checkRequirement(multiblock: Multiblock, sign: Sign, input: Inventory): Boolean {
		return input.containsAtLeast(ingredient, amount)
	}

	override fun consume(multiblock: Multiblock, sign: Sign, input: Inventory) {
		val items = input.filter { it?.isSimilar(ingredient) == true }

		var remaining = amount

		for (itemStack in items) {
			val toRemove = min(remaining, itemStack.amount)

			if (toRemove == itemStack.amount) {
				input.removeItem(itemStack)
			} else {
				itemStack.amount -= toRemove
			}

			remaining -= toRemove

			if (remaining == 0) break
		}
	}
}
