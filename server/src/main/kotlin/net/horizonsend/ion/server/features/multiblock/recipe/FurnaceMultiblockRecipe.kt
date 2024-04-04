package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.ItemConsumable
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.ResourceIngredient
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Recipes that use both slots of a furnace
 *
 * Do NOT try to use this on a non-furnace inventory
 **/
class FurnaceMultiblockRecipe<out T: Multiblock>(
	override val multiblock: T,
	val time: Long,
	val smelting: MultiblockRecipeIngredient,
	val fuel: MultiblockRecipeIngredient,
	private val resources: List<ResourceIngredient> = listOf(),
	override val result: ItemStack
) : MultiblockRecipe<T> {
	private val cookTimeMultiplier = 200.0 / time.toDouble()

	override fun execute(sign: Sign, inventory: Inventory) {
		inventory as FurnaceInventory

		if ((inventory.result?.amount ?: 0) >= result.maxStackSize) return

		// Return if enough ingredients are not present
		if (!smelting.checkRequirement(multiblock, sign, inventory.smelting)) return
		if (!fuel.checkRequirement(multiblock, sign, inventory.fuel)) return

		if (resources.any { !it.checkRequirement(multiblock, sign, null) }) return

		inventory.result?.add(1) ?: run { inventory.result = result.asQuantity(1) }

		if (smelting is ItemConsumable) smelting.consume(multiblock, sign, inventory.smelting!!)
		if (fuel is ItemConsumable) fuel.consume(multiblock, sign, inventory.fuel!!)

		val holder = inventory.holder!!
		holder.cookTimeTotal = 200
		holder.cookTime = 0
		holder.cookSpeedMultiplier = cookTimeMultiplier
		holder.burnTime = 200

		resources.forEach { it.consume(multiblock, sign) }
	}

	override fun matches(sign: Sign, inventory: Inventory): Boolean {
		inventory as FurnaceInventory

		if (resources.any { !it.checkRequirement(multiblock, sign, null) }) return false
		if (!smelting.checkRequirement(multiblock, sign, inventory.smelting)) return false

		return fuel.checkRequirement(multiblock, sign, inventory.fuel)
	}
}
