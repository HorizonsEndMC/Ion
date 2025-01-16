package net.horizonsend.ion.server.features.multiblock.newcrafting

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.MultiblockRecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.NewMultiblockRecipe
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import kotlin.reflect.KClass

object MultiblockRecipeRegistry : IonServerComponent() {
	val recipes = mutableListOf<NewMultiblockRecipe<*>>()
	val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, NewMultiblockRecipe<*>>()



	fun <I: MultiblockRecipeEnviornment, R: NewMultiblockRecipe<I>> register(recipe: R): R {
		recipes.add(recipe)
		byMultiblock[recipe.entityType].add(recipe)

		return recipe
	}
}
