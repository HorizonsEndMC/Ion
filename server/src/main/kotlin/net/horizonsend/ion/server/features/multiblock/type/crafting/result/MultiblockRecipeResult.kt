package net.horizonsend.ion.server.features.multiblock.type.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.MultiblockRecipe
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

interface MultiblockRecipeResult {
	fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean

	fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign)
}
