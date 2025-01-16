package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe

import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.Consumable
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

abstract class NewMultiblockRecipe<E: RecipeEnviornment>(val identifier: String, val entityType: KClass<out RecipeProcessingMultiblockEntity<E>>) {
	protected abstract val requirements: Collection<RequirementHolder<E, *, *>>

	fun getConsumableRequirements(): Collection<RequirementHolder<E, *, Consumable<*, E>>> = requirements
		.filter { holder -> holder.requirement is Consumable<* , *> }
		.filterIsInstance<RequirementHolder<E, *, Consumable<*, E>>>()

	fun getItemRequirements(): Collection<RequirementHolder<E, ItemStack?, *>> = requirements
		.filter { it.dataTypeClass == ItemStack::class.java }
		.filterIsInstance<RequirementHolder<E, ItemStack?, *>>()

	fun verifyAllRequirements(enviornment: E): Boolean = getAllRequirements().all { holder ->
		holder.checkRequirement(enviornment)
	}

	fun getAllRequirements(): Collection<RequirementHolder<E, *, *>> = requirements

	abstract fun assemble(input: E)
}
