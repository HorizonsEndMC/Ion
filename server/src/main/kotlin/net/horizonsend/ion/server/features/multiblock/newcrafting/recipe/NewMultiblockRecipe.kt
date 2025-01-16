package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe

import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.MultiblockRecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.RecipeResult
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

abstract class NewMultiblockRecipe<E: MultiblockRecipeEnviornment>(val entityType: KClass<RecipeProcessingMultiblockEntity<E>>) {
	protected abstract val requirements: Collection<RequirementHolder<E, *>>

	fun getItemRequirements(): Collection<RequirementHolder<E, ItemStack?>> = requirements.filter {

	}

	fun getAllRequirements(): Collection<RequirementHolder<E, *>> = requirements

	fun ensureRequirementsAvailable(input: E): Boolean

	abstract fun assemble(input: E): RecipeResult<E>
}
