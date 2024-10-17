package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ItemIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.PowerIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.result.MultiblockRecipeResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import kotlin.reflect.KClass

/**
 * A multiblock recipe that takes one ingredient, and produces one result
 * Usually furnace multiblocks with prismarine crystals occupying the bottom slot
 *
 * @param smelting, the input resource
 * @param result, the result of the recipe
 **/
class ProcessingMultiblockRecipe<T: MultiblockEntity>(
	multiblock: EntityMultiblock<T>,
	multiblockEntityClass: KClass<T>,
	val smelting: MultiblockRecipeIngredient<T>,
	override val result: MultiblockRecipeResult<T>,
	private val resources: List<PowerIngredient<T>> = listOf(),
) : MultiblockRecipe<T>(multiblock, multiblockEntityClass) {
	override fun finalize(context: RecipeExecutionContext<T>) {
		result.execute(context)
	}

	override fun getLabeledInventories(context: RecipeExecutionContext<T>): Map<String, Inventory> {
		return mapOf(ItemIngredient.MAIN_FURNACE_STRING to context.entity.getInventory(0, 0, 0) as FurnaceInventory)
	}

	override fun checkResourcesAvailable(context: RecipeExecutionContext<T>): Boolean {
		return smelting.checkRequirement(context) && resources.all { it.checkRequirement(context) }
	}

	override fun consumeResources(context: RecipeExecutionContext<T>): Boolean {
		return smelting.consumeIngredient(context) && resources.all { it.consumeIngredient(context) }
	}
}
