package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

/**
 * Wrap multiple result actions
 **/
class MultiRecipeResult<T: MultiblockEntity>(
	val main: MultiblockRecipeResult<T>,
	vararg others: ActionResult<T>
) : MultiblockRecipeResult<T> {
	private val actions = others.toList()

	override fun canFit(context: RecipeExecutionContext<T>): Boolean {
		return main.canFit(context)
	}

	override fun execute(context: RecipeExecutionContext<T>) {
		main.execute(context)
		actions.forEach { it.execute(context) }
	}
}
