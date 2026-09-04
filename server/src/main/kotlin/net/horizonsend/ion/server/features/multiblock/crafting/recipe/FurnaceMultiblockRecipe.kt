package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnvirornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultExecutionEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import kotlin.reflect.KClass

/**
 * A multiblock recipe that uses a furnace inventory.
 **/
class FurnaceMultiblockRecipe(
	key: IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<FurnaceEnvirornment>>,
	clazz: KClass<out RecipeProcessingMultiblockEntity<FurnaceEnvirornment>>,
	smeltingItem: ItemRequirement?,
	fuelItem: ItemRequirement?,
	power: PowerRequirement<FurnaceEnvirornment>,
	private val result: ResultHolder<FurnaceEnvirornment, ItemResult<FurnaceEnvirornment>>
) : MultiblockRecipe<FurnaceEnvirornment>(key, clazz) {

	override val requirements: Collection<RequirementHolder<FurnaceEnvirornment, *, *>> = listOf(
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

	override fun assemble(environment: FurnaceEnvirornment): Boolean {
		if (!verifyAllRequirements(environment, true)) return false
		if (!result.verifySpace(environment)) return false

		val resultEnvironment = ResultExecutionEnvironment(environment, this)

		result.buildTransaction(environment, resultEnvironment)

		try {
			resultEnvironment.requirements.forEach { requirement -> requirement.consume(environment) }
		} catch (e: Throwable) {
			IonServer.slF4JLogger.error("There was an error executing multiblock recipe $key: ${e.message}")
			e.printStackTrace()
			return false
		}

		// Once ingredients have been sucessfully consumed, execute the result
		val executionResult = resultEnvironment.executeResult()
		result.executeCallbacks(environment, executionResult)
		return true
	}
}
