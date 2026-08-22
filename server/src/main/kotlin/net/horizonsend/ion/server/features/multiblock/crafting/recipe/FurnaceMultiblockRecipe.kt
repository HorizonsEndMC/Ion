package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultExecutionEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import kotlin.reflect.KClass

/**
 * A multiblock recipe that uses a furnace inventory.
 **/
class FurnaceMultiblockRecipe(
	key: IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<FurnaceEnviornment>>,
	clazz: KClass<out RecipeProcessingMultiblockEntity<FurnaceEnviornment>>,
	smeltingItem: ItemRequirement?,
	fuelItem: ItemRequirement?,
	power: PowerRequirement<FurnaceEnviornment>,
	private val result: ResultHolder<FurnaceEnviornment, ItemResult<FurnaceEnviornment>>
) : MultiblockRecipe<FurnaceEnviornment>(key, clazz) {

	override val requirements: Collection<RequirementHolder<FurnaceEnviornment, *, *>> = listOf(
		// Furnace smelting item
		RequirementHolder.itemConsumable(
			getter = { it.getInputItem(0) },
			requirement = smeltingItem ?: ItemRequirement.legacy(),
			{ it.getInputItemSlotModifier(0) }
		),
		// Furnace fuel item
		RequirementHolder.itemConsumable(
			getter = { it.getInputItem(1) },
			requirement = fuelItem ?: ItemRequirement.legacy(),
			{ it.getInputItemSlotModifier(1) }
		),
		// Power requirement
		RequirementHolder.simpleConsumable(
			{ it.powerStorage.getPower() },
			power
		)
	)

	override fun assemble(enviornment: FurnaceEnviornment) {
		if (!verifyAllRequirements(enviornment)) result
		if (!result.verifySpace(enviornment)) return

		val resultEnviornment = ResultExecutionEnviornment(enviornment, this)

		result.buildTransaction(enviornment, resultEnviornment)

		try {
			resultEnviornment.requirements.forEach { requirement -> requirement.consume(enviornment) }
		} catch (e: Throwable) {
			IonServer.slF4JLogger.error("There was an error executing multiblock recipe $key: ${e.message}")
			e.printStackTrace()
			return
		}

		// Once ingredients have been sucessfully consumed, execute the result
		val executionResult = resultEnviornment.executeResult()
		result.executeCallbacks(enviornment, executionResult)
	}
}
