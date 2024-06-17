package net.horizonsend.ion.server.features.multiblock.type.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.crafting.result.MultiblockRecipeResult
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

interface MultiblockRecipe<out T: Multiblock> {
	val multiblock: T
	val result: MultiblockRecipeResult

	fun matches(sign: Sign, inventory: Inventory): Boolean

	fun execute(sign: Sign, inventory: Inventory) // TODO redesign this on multiblock rewrite branch
}
