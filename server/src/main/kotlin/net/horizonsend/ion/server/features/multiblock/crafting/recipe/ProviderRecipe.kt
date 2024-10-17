package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.crafting.result.MultiblockRecipeResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import org.bukkit.inventory.Inventory
import kotlin.reflect.KClass

class ProviderRecipe<T: MultiblockEntity>(
	multiblock: EntityMultiblock<T>,
	entityClass: KClass<out T>,
	override val result: MultiblockRecipeResult<T>,
	private val getLabeledInventories: RecipeExecutionContext<T>.() -> Map<String, Inventory>,
	private val checkResourcesAvailable: RecipeExecutionContext<T>.() -> Boolean,
	private val consumeResources: RecipeExecutionContext<T>.() -> Boolean
) : MultiblockRecipe<T>(multiblock, entityClass) {

	override fun consumeResources(context: RecipeExecutionContext<T>): Boolean {
		return consumeResources.invoke(context)
	}

	override fun checkResourcesAvailable(context: RecipeExecutionContext<T>): Boolean {
		return checkResourcesAvailable.invoke(context)
	}

	override fun getLabeledInventories(context: RecipeExecutionContext<T>): Map<String, Inventory> {
		return getLabeledInventories.invoke(context)
	}

	override fun finalize(context: RecipeExecutionContext<T>) {
		result.execute(context)
	}
}
