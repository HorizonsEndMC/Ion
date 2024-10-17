package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import java.time.Duration

/**
 * This item will convert into the result custom item after the amount of ticks provided (Assuming no lag and a 200 tick interval on the furnace)
 **/
class ProgressResult<T: MultiblockEntity>(
	val duration: Duration,
	val finalResult: MultiblockRecipeResult<T>
) : MultiblockRecipeResult<T> {
	override fun canFit(context: RecipeExecutionContext<T>): Boolean {
		return finalResult.canFit(context)
	}

	override fun execute(context: RecipeExecutionContext<T>) {
		val multiblock = context.entity as ProgressMultiblock

		if (multiblock.tickProgress(duration)) {
			multiblock.progressManager.reset()
			finalResult.execute(context)
		}
	}
}
