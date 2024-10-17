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
 * Recipes that use both slots of a furnace
 *
 * Do NOT try to use this on a non-furnace inventory
 **/
class FurnaceMultiblockRecipe<T: MultiblockEntity>(
	multiblock: EntityMultiblock<T>,
	multiblockEntityClass: KClass<out T>,
	val smelting: MultiblockRecipeIngredient<T>,
	val fuel: MultiblockRecipeIngredient<T>,
	private val resources: List<PowerIngredient<T>> = listOf(),
	override val result: MultiblockRecipeResult<T>
) : MultiblockRecipe<T>(multiblock, multiblockEntityClass) {
	override fun finalize(context: RecipeExecutionContext<T>) {
		result.execute(context)
	}

	override fun getLabeledInventories(context: RecipeExecutionContext<T>): Map<String, Inventory> {
		return mapOf(ItemIngredient.MAIN_FURNACE_STRING to context.entity.getInventory(0, 0, 0) as FurnaceInventory)
	}

	override fun checkResourcesAvailable(context: RecipeExecutionContext<T>): Boolean {
		return smelting.checkRequirement(context)
			&& fuel.checkRequirement(context)
			&& resources.all { it.checkRequirement(context) }
	}

	override fun consumeResources(context: RecipeExecutionContext<T>): Boolean {
		return smelting.consumeIngredient(context)
			&& fuel.consumeIngredient(context)
			&& resources.all { it.consumeIngredient(context) }
	}
}
