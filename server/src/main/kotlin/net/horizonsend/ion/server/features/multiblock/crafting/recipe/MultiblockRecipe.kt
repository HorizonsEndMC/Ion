package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.crafting.result.MultiblockRecipeResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import org.bukkit.inventory.Inventory
import kotlin.reflect.KClass

abstract class MultiblockRecipe<T: MultiblockEntity>(val multiblock: EntityMultiblock<T>, val entityClass: KClass<out T>) {
	abstract val result: MultiblockRecipeResult<T>

	abstract fun finalize(context: RecipeExecutionContext<T>)

	abstract fun getLabeledInventories(context: RecipeExecutionContext<T>): Map<String, Inventory>

	abstract fun checkResourcesAvailable(context: RecipeExecutionContext<T>): Boolean

	abstract fun consumeResources(context: RecipeExecutionContext<T>): Boolean

	fun canExecute(entity: T): Boolean = checkResourcesAvailable(RecipeExecutionContext(this, entity))
}
