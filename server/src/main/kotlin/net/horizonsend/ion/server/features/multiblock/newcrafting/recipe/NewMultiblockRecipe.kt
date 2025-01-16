package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe

import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.MultiblockRecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

abstract class NewMultiblockRecipe<E: MultiblockRecipeEnviornment>(val entityType: KClass<RecipeProcessingMultiblockEntity<E>>) {
	protected abstract val requirements: Collection<RequirementHolder<E, *>>

	fun getItemRequirements(): Collection<RequirementHolder<E, ItemStack?>> = requirements
		.filter { it.dataTypeClass == ItemStack::class.java }
		.filterIsInstance<RequirementHolder<E, ItemStack?>>()

	fun verifyAllRequirements(enviornment: E): Boolean = getAllRequirements().all { holder -> holder.checkRequirement(enviornment) }

	fun getAllRequirements(): Collection<RequirementHolder<E, *>> = requirements

	fun ensureRequirementsAvailable(input: E): Boolean

	abstract fun assemble(input: E)
}
