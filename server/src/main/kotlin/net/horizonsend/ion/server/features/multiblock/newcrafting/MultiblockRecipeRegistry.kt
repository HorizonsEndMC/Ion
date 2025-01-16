package net.horizonsend.ion.server.features.multiblock.newcrafting

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.NewMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import kotlin.reflect.KClass

object MultiblockRecipeRegistry : IonServerComponent() {
	val recipes = mutableListOf<NewMultiblockRecipe<*>>()
	val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, NewMultiblockRecipe<*>>()

	val URANIUM_ENRICHMENT = register(FurnaceMultiblockRecipe(
		identifier = "URANIUM_ENRICHMENT",
		clazz = CentrifugeMultiblock.CentrifugeMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.URANIUM),
		fuelItem = null,
		power = PowerRequirement(100),
		result = ItemResult.simpleResult(CustomItemRegistry.ENRICHED_URANIUM)
	))

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
