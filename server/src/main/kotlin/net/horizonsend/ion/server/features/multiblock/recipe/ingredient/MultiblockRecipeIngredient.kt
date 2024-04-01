package net.horizonsend.ion.server.features.multiblock.recipe.ingredient

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

interface MultiblockRecipeIngredient {
	fun checkRequirement(multiblock: Multiblock, sign: Sign, input: Inventory): Boolean
	fun consume(multiblock: Multiblock, sign: Sign, input: Inventory)
}
