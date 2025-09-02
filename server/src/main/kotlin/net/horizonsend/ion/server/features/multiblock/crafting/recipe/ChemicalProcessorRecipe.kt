package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.ChemicalProcessorEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.E2Requirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.FluidRecipeRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder.Companion.anySlot
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.FluidResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultExecutionEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.type.fluid.ChemicalProcessorMultiblock.ChemicalProcessorEntity
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class ChemicalProcessorRecipe(
	key: IonRegistryKey<MultiblockRecipe<*>, out ChemicalProcessorRecipe>,
	val itemRequirement: ItemRequirement?,
	val fluidRequirementOne: FluidRecipeRequirement<ChemicalProcessorEnviornment>?,
	val fluidRequirementTwo: FluidRecipeRequirement<ChemicalProcessorEnviornment>?,
	val e2Requirement: E2Requirement<ChemicalProcessorEnviornment>?,

	val fluidResultOne: FluidResult<ChemicalProcessorEnviornment>?,
	val fluidResultTwo: FluidResult<ChemicalProcessorEnviornment>?,
	val fluidResultPollutionResult: FluidResult<ChemicalProcessorEnviornment>?,
	val itemResult: ResultHolder<ChemicalProcessorEnviornment, ItemResult<ChemicalProcessorEnviornment>>?,
	val resultSleepTicks: Int
) : MultiblockRecipe<ChemicalProcessorEnviornment>(key, ChemicalProcessorEntity::class) {
	override val requirements: Collection<RequirementHolder<ChemicalProcessorEnviornment, *, *>> = listOfNotNull(
		// Input item requirement
		itemRequirement?.let { anySlot(it, template(Component.text("Missing {0}", NamedTextColor.RED), itemRequirement.asItemStack()?.displayNameComponent)) },

		// Fluid one
		fluidRequirementOne?.let {
			RequirementHolder.simpleConsumable(
				{ it.fluidStore.getNamedStorage(fluidRequirementOne.storeName)?.getContents() ?: FluidStack.empty() },
				fluidRequirementOne,
				template(Component.text("Missing {0}", NamedTextColor.RED), fluidRequirementOne.type.getValue().displayName)
			)
		},

		// Fluid two
		fluidRequirementTwo?.let {
			RequirementHolder.simpleConsumable(
				{ it.fluidStore.getNamedStorage(fluidRequirementTwo.storeName)?.getContents() ?: FluidStack.empty() },
				fluidRequirementTwo,
				template(Component.text("Missing {0}", NamedTextColor.RED), fluidRequirementTwo.type.getValue().displayName)
			)
		},

		// Fluid two
		e2Requirement?.let {
			RequirementHolder.simpleConsumable(
				{ it.getAvailablePower(e2Requirement.amount) },
				e2Requirement,
				Component.text("Insufficient E2", NamedTextColor.RED)
			)
		}
	)

	override fun assemble(enviornment: ChemicalProcessorEnviornment): Boolean {
		if (!verifyAllRequirements(enviornment, true)) return false
		if (itemResult != null && !itemResult.verifySpace(enviornment)) return false
		if (fluidResultOne != null && !fluidResultOne.verifySpace(enviornment)) return false
		if (fluidResultTwo != null && !fluidResultTwo.verifySpace(enviornment)) return false
		if (fluidResultPollutionResult != null && !fluidResultPollutionResult.verifySpace(enviornment)) return false

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
			return false
		}

		// Once ingredients have been sucessfully consumed, execute the result
		val executionResult = resultEnviornment.executeResult()
		itemResult?.executeCallbacks(enviornment, executionResult)
		enviornment.multiblock.tickingManager.sleepForTicks(resultSleepTicks)
		return true
	}
}
