package net.horizonsend.ion.server.features.multiblock.type

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.MultiblockRecipes
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceEventHandler
import net.horizonsend.ion.server.features.multiblock.type.crafting.MultiblockRecipes
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.FurnaceEventHandler
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

interface FurnaceMultiblock {
	fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign)

	/**
	 * Handle the execution of a recipe
	 **/
	fun handleRecipe(multiblock: Multiblock, event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isCancelled = true

		val recipe = MultiblockRecipes.getRecipe(multiblock, sign, furnace.inventory) ?: run {
			event.burnTime = 500
			return
		}

		if (recipe is FurnaceEventHandler) recipe.handleFurnaceEvent(event, furnace, sign)
		recipe.execute(sign, furnace.inventory)

		furnace.cookTime = 0
	}
}
