package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.crafting.MultiblockRecipes
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity

/**
 * Multiblock which processes multiblock recipes
 **/
interface RecipeEntity : ProgressMultiblock, SyncTickingMultiblockEntity {
	fun <T: MultiblockEntity> T.getRecipe(): MultiblockRecipe<T>? {
		return MultiblockRecipes.getRecipe(this)
	}

	override fun tick() {
		return
		executeRecipes()
	}

	fun executeRecipes() {
		this as MultiblockEntity
		val recipe = this.getRecipe()

		if (recipe == null) {
			tickingManager.sleep(500)
			return
		}

		if (recipe.canExecute(this)) return

		val context = RecipeExecutionContext(recipe, this)
		context.execute()
	}
}
