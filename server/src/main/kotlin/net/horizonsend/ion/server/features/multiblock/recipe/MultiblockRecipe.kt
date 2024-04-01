package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.MultiblockRecipeIngredient
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface MultiblockRecipe<T: Multiblock> {
	val multiblock: T
	val result: ItemStack

	fun execute(multiblock: T, sign: Sign, inventory: Inventory) // TODO redesign this on multiblock rewrite branch

	fun matches(multiblock: T, sign: Sign, inventory: Inventory): Boolean

	fun checkAndConsume(ingredient: MultiblockRecipeIngredient, multiblock: T, sign: Sign, inventory: Inventory): Boolean {
		if (!ingredient.checkRequirement(multiblock, sign, inventory)) return false
		ingredient.consume(multiblock, sign, inventory)

		return true
	}
}
