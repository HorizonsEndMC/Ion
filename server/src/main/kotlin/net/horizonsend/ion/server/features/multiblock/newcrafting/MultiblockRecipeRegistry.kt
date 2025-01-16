package net.horizonsend.ion.server.features.multiblock.newcrafting

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.NewMultiblockRecipe
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import kotlin.reflect.KClass

object MultiblockRecipeRegistry : IonServerComponent() {
	val recipes = mutableListOf<NewMultiblockRecipe<*>>()
	val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, NewMultiblockRecipe<*>>()



	fun <I: RecipeEnviornment, R: NewMultiblockRecipe<I>> register(recipe: R): R {
		recipes.add(recipe)
		byMultiblock[recipe.entityType].add(recipe)

		return recipe
	}

	fun <E: RecipeEnviornment> getRecipesFor(entity: RecipeProcessingMultiblockEntity<E>): Collection<NewMultiblockRecipe<E>> {
		@Suppress("UNCHECKED_CAST")
		return byMultiblock[entity::class] as Collection<NewMultiblockRecipe<E>>
	}
}
