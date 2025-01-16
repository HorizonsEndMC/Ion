package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe

import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.IndustryMultiblockEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.RecipeResult
import kotlin.reflect.KClass

/**
 * A multiblock recipe that uses a furnace inventory.
 *
 * @param smeltingItem Requirement for the item in the top slot. If it is null, there will be a requirement for this slot to be empty.
 * @param fuelitem Requirement for the item in the bottom slot. If it is null, there will be a requirement for this slot to be empty.
 **/
class IndustryMultiblockRecipe(
	clazz: KClass<RecipeProcessingMultiblockEntity<IndustryMultiblockEnviornment>>,
	smeltingItem: ItemRequirement?,
	fuelitem: ItemRequirement?,
	power: PowerRequirement,
	val result: RecipeResult<IndustryMultiblockEnviornment>
) : NewMultiblockRecipe<IndustryMultiblockEnviornment>(clazz) {

	override val requirements: Collection<RequirementHolder<IndustryMultiblockEnviornment, *>> = listOf(
		RequirementHolder({ it.getItem(0) }, smeltingItem ?: ItemRequirement.empty()),
		RequirementHolder({ it.getItem(1) }, fuelitem ?: ItemRequirement.empty()),
		RequirementHolder({ it.powerStorage.getPower() }, power)
	)

	override fun assemble(input: IndustryMultiblockEnviornment): RecipeResult<IndustryMultiblockEnviornment> {

	}
}
