package net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.ingredient.ResourceIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.result.FluidResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.result.MultiblockRecipeResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import org.bukkit.inventory.FurnaceInventory
import kotlin.reflect.KClass

class LegacyIndustryRecipe(
	entity: KClass<out MultiblockEntity>,
	private val result: MultiblockRecipeResult,
	private val smelting: MultiblockRecipeIngredient?,
	private val fuel: MultiblockRecipeIngredient?,
	private vararg val resources: ResourceIngredient,
) : NewRecipe(entity) {

	private fun getFurnace(context: ExecutionContext): FurnaceInventory {
		return context.entity.getInventory(0, 0, 0) as? FurnaceInventory ?: throw IllegalStateException("Multiblock integrity should be ensured before execution")
	}

	override fun checkResourcesAvailable(context: ExecutionContext): Boolean {
		if (smelting?.check(context) == false) return false
		if (fuel?.check(context) == false) return false
		return resources.all { it.check(context) }
	}

	override fun checkSpaceAvailable(context: ExecutionContext): Boolean {
		val furnace = getFurnace(context)

		return when (result) {
			is ItemResult -> {
				val resultSlot = furnace.result ?: return true
				val resultItem = result.assemble()
				if (resultSlot.isSimilar(resultItem)) return false
				if (resultItem.amount + resultSlot.amount > resultSlot.maxStackSize) return false
				true
			}
			is FluidResult -> context.entity is FluidStoringEntity && context.entity.getCapacityFor(result.fluid.type) >= result.fluid.amount
			else -> false
		}
	}

	override fun execute(context: ExecutionContext) {
		val furnace = getFurnace(context)

	}
}
