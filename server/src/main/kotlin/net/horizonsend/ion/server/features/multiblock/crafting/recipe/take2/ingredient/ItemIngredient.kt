package net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.NewRecipe
import org.bukkit.inventory.ItemStack

class ItemIngredient(private val item: ItemStack) : MultiblockRecipeIngredient() {
	override fun check(context: NewRecipe.ExecutionContext): Boolean {
		return true
	}

	override fun consume(context: NewRecipe.ExecutionContext) {

	}
}
