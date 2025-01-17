package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe

import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * A multiblock recipe that uses a furnace inventory.
 *
 * @param smeltingItem Requirement for the item in the top slot. If it is null, there will be a requirement for this slot to be empty.
 * @param fuelItem Requirement for the item in the bottom slot. If it is null, there will be a requirement for this slot to be empty.
 **/
class FurnaceMultiblockRecipe(
	identifier: String,
	clazz: KClass<out RecipeProcessingMultiblockEntity<FurnaceEnviornment>>,
	smeltingItem: ItemRequirement?,
	fuelItem: ItemRequirement?,
	power: PowerRequirement,
	result: ItemResult<FurnaceEnviornment>
) : NewMultiblockRecipe<FurnaceEnviornment>(identifier, clazz) {
	val callbacks = mutableListOf<Consumer<FurnaceEnviornment>>()
	private val result = ResultHolder.of(result)

	override val requirements: Collection<RequirementHolder<FurnaceEnviornment, *, *>> = listOf(
		// Furnace smelting item
		RequirementHolder.itemConsumable(
			getter = { it.getItem(0) },
			requirement = smeltingItem ?: ItemRequirement.legacy(),
			{ SlotModificationWrapper.furnaceSmelting(it.furnaceInventory) }
		),
		// Furnace fuel item
		RequirementHolder.itemConsumable(
			getter = { it.getItem(1) },
			requirement = fuelItem ?: ItemRequirement.legacy(),
			{ SlotModificationWrapper.furnaceFuel(it.furnaceInventory) }
		),
		// Power requirement
		RequirementHolder.simpleConsumable(
			{ it.powerStorage.getPower() },
			power
		)
	)

	fun withCallback(consumer: Consumer<FurnaceEnviornment>): FurnaceMultiblockRecipe {
		callbacks.add(consumer)
		return this
	}

	override fun assemble(enviornment: FurnaceEnviornment) {
		if (!verifyAllRequirements(enviornment)) result
		if (!result.verifySpace(enviornment)) return

		result
			.filterConsumedIngredients(enviornment, getAllRequirements())
			.forEach { requirement -> requirement.consume(enviornment) }

		result.execute(enviornment)
		callbacks.forEach { consumer -> consumer.accept(enviornment) }
	}
}
