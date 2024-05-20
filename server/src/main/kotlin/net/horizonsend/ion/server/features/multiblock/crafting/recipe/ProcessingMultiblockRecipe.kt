package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ItemConsumable
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ResourceIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.result.MultiblockRecipeResult
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory

/**
 * A multiblock recipe that takes one ingredient, and produces one result
 * Usually furnace multiblocks with prismarine crystals occupying the bottom slot
 *
 * @param time, in ticks
 * @param smelting, the input resource
 * @param result, the result of the recipe
 **/
class ProcessingMultiblockRecipe<T: Multiblock>(
	override val multiblock: T,
	val smelting: MultiblockRecipeIngredient,
	override val result: MultiblockRecipeResult,
	private val resources: List<ResourceIngredient> = listOf(),
) : MultiblockRecipe<T>, FurnaceEventHandler {
	init {
	    require(multiblock is FurnaceMultiblock)
	}

	override fun matches(sign: Sign, inventory: Inventory): Boolean {
		inventory as FurnaceInventory
		return smelting.checkRequirement(multiblock, sign, inventory.smelting)
	}

	override fun execute(sign: Sign, inventory: Inventory) {
		inventory as FurnaceInventory

		val holder = inventory.holder!!
		if (!(holder.blockData as org.bukkit.block.data.type.Furnace).isLit) return

		if (!result.canFit(this, inventory, sign)) return

		// Return if enough ingredients are not present
		if (!smelting.checkRequirement(multiblock, sign, inventory.smelting)) return

		if (resources.any { !it.checkRequirement(multiblock, sign, null) }) return

		result.execute(this, inventory, sign)

		if (smelting is ItemConsumable) smelting.consume(multiblock, sign, inventory.smelting!!)

		resources.forEach { it.consume(multiblock, sign) }
	}

	override fun handleFurnaceEvent(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 200
		event.isCancelled = false
	}
}
