package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ItemConsumable
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ResourceIngredient
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Furnace
import org.bukkit.event.inventory.FurnaceBurnEvent
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
) : MultiblockRecipe<T>, FurnaceEventHandler {
	private val cookTimeMultiplier = 200.0 / time.toDouble()

	override fun matches(sign: Sign, inventory: Inventory): Boolean {
		inventory as FurnaceInventory

		if (resources.any { !it.checkRequirement(multiblock, sign, null) }) return false
		if (!smelting.checkRequirement(multiblock, sign, inventory.smelting)) return false

		return fuel.checkRequirement(multiblock, sign, inventory.fuel)
	}

	override fun execute(sign: Sign, inventory: Inventory) {
		inventory as FurnaceInventory

		val holder = inventory.holder!!
		if (!(holder.blockData as Furnace).isLit) return

		if ((inventory.result?.amount ?: 0) >= result.maxStackSize) return

		// Return if enough ingredients are not present
		if (!smelting.checkRequirement(multiblock, sign, inventory.smelting)) return
		if (!fuel.checkRequirement(multiblock, sign, inventory.fuel)) return

		if (resources.any { !it.checkRequirement(multiblock, sign, null) }) return

		inventory.result?.add(1) ?: run { inventory.result = result.asQuantity(1) }

		if (smelting is ItemConsumable) smelting.consume(multiblock, sign, inventory.smelting!!)
		if (fuel is ItemConsumable) fuel.consume(multiblock, sign, inventory.fuel!!)

		resources.forEach { it.consume(multiblock, sign) }
	}

	override fun handleFurnaceEvent(event: FurnaceBurnEvent, furnace: org.bukkit.block.Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 200
		event.isCancelled = false
		furnace.cookSpeedMultiplier = cookTimeMultiplier
	}
}
