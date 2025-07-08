package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.ChemicalProcessorEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.FluidRecipeRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder.Companion.anySlot
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.FluidResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultExecutionEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.ChemicalProcessorMultiblock.ChemicalProcessorEntity
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult.ValidatorSuccessEmpty.result

class ChemicalProcessorRecipe(
	key: IonRegistryKey<MultiblockRecipe<*>, out ChemicalProcessorRecipe>,
	itemRequirement: ItemRequirement?,
	val fluidRequirementOne: FluidRecipeRequirement<ChemicalProcessorEnviornment>?,
	val fluidRequirementTwo: FluidRecipeRequirement<ChemicalProcessorEnviornment>?,

	val fluidResultOne: FluidResult<ChemicalProcessorEnviornment>?,
	val fluidResultTwo: FluidResult<ChemicalProcessorEnviornment>?,
	val fluidResultPollutionResult: FluidResult<ChemicalProcessorEnviornment>?,
	val itemResult: ResultHolder<ChemicalProcessorEnviornment, ItemResult<ChemicalProcessorEnviornment>>?,

	) : MultiblockRecipe<ChemicalProcessorEnviornment>(key, ChemicalProcessorEntity::class) {
	override val requirements: Collection<RequirementHolder<ChemicalProcessorEnviornment, *, *>> = listOfNotNull(
		// Input item requirement
		itemRequirement?.let(::anySlot),

		// Fluid one
		fluidRequirementOne?.let {
			RequirementHolder.simpleConsumable(
				{ it.fluidStore.getNamedStorage(fluidRequirementOne.storeName)?.getContents() ?: FluidStack.empty() },
				fluidRequirementOne
			)
		},

		// Fluid two
		fluidRequirementTwo?.let {
			RequirementHolder.simpleConsumable(
				{ it.fluidStore.getNamedStorage(fluidRequirementTwo.storeName)?.getContents() ?: FluidStack.empty() },
				fluidRequirementTwo
			)
		}
	)

	override fun assemble(enviornment: ChemicalProcessorEnviornment) {
		if (!verifyAllRequirements(enviornment)) result
		if (itemResult != null && !itemResult.verifySpace(enviornment)) return
		if (fluidResultOne != null && !fluidResultOne.verifySpace(enviornment)) return
		if (fluidResultTwo != null && !fluidResultTwo.verifySpace(enviornment)) return
		if (fluidResultPollutionResult != null && !fluidResultPollutionResult.verifySpace(enviornment)) return

		val resultEnviornment = ResultExecutionEnviornment(enviornment, this)

		if (fluidResultOne != null) resultEnviornment.addResult(fluidResultOne.resultConsumer)
		if (fluidResultTwo != null) resultEnviornment.addResult(fluidResultTwo.resultConsumer)
		if (fluidResultPollutionResult != null) resultEnviornment.addResult(fluidResultPollutionResult.resultConsumer)

		itemResult?.buildTransaction(enviornment, resultEnviornment)

		try {
			resultEnviornment.requirements.forEach { requirement -> requirement.consume(enviornment) }
		} catch (e: Throwable) {
			IonServer.slF4JLogger.error("There was an error executing multiblock recipe $key: ${e.message}")
			e.printStackTrace()
			return
		}

		// Once ingredients have been sucessfully consumed, execute the result
		val executionResult = resultEnviornment.executeResult()
		itemResult?.executeCallbacks(enviornment, executionResult)
	}
}
