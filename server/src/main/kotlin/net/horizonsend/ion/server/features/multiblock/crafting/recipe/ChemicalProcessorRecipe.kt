package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.ChemicalProcessorEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.FluidRecipeRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder.Companion.anySlot
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.FluidResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultExecutionEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.type.fluid.ChemicalProcessorMultiblock.ChemicalProcessorEntity
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class ChemicalProcessorRecipe(
	key: IonRegistryKey<MultiblockRecipe<*>, out ChemicalProcessorRecipe>,
	val itemRequirement: ItemRequirement?,
	val fluidRequirementOne: FluidRecipeRequirement<ChemicalProcessorEnvironment>?,
	val fluidRequirementTwo: FluidRecipeRequirement<ChemicalProcessorEnvironment>?,

	val fluidResultOne: FluidResult<ChemicalProcessorEnvironment>?,
	val fluidResultTwo: FluidResult<ChemicalProcessorEnvironment>?,
	val fluidResultPollutionResult: FluidResult<ChemicalProcessorEnvironment>?,
	val itemResult: ResultHolder<ChemicalProcessorEnvironment, ItemResult<ChemicalProcessorEnvironment>>?,

	val resultSleepTicks: Int
	) : MultiblockRecipe<ChemicalProcessorEnvironment>(key, ChemicalProcessorEntity::class) {
	override val requirements: Collection<RequirementHolder<ChemicalProcessorEnvironment, *, *>> = listOfNotNull(
		// Input item requirement
		itemRequirement?.let { anySlot(it, template(Component.text("Missing {0}", NamedTextColor.RED), itemRequirement.asItemStack()?.displayNameComponent)) },

		// Fluid one
		fluidRequirementOne?.let {
			RequirementHolder.simpleConsumable(
				{ it.fluidStore.getNamedStorage(fluidRequirementOne.storeName)?.getContents() ?: FluidStack.empty() },
				fluidRequirementOne,
				template(Component.text("Missing {0}", NamedTextColor.RED), fluidRequirementOne.asFluidStack().getDisplayName()),
			)
		},

		// Fluid two
		fluidRequirementTwo?.let {
			RequirementHolder.simpleConsumable(
				{ it.fluidStore.getNamedStorage(fluidRequirementTwo.storeName)?.getContents() ?: FluidStack.empty() },
				fluidRequirementTwo,
				template(Component.text("Missing {0}", NamedTextColor.RED), fluidRequirementTwo.asFluidStack().getDisplayName())
			)
		}
	)

	override fun assemble(environment: ChemicalProcessorEnvironment): Boolean {
		if (!verifyAllRequirements(environment, true)) return false
		if (itemResult != null && !itemResult.verifySpace(environment)) return false
		if (fluidResultOne != null && !fluidResultOne.verifySpace(environment)) return false
		if (fluidResultTwo != null && !fluidResultTwo.verifySpace(environment)) return false
		if (fluidResultPollutionResult != null && !fluidResultPollutionResult.verifySpace(environment)) return false

		val resultEnvironment = ResultExecutionEnvironment(environment, this)

		if (fluidResultOne != null) resultEnvironment.addResult(fluidResultOne.resultConsumer)
		if (fluidResultTwo != null) resultEnvironment.addResult(fluidResultTwo.resultConsumer)
		if (fluidResultPollutionResult != null) resultEnvironment.addResult(fluidResultPollutionResult.resultConsumer)

		itemResult?.buildTransaction(environment, resultEnvironment)

		try {
			resultEnvironment.requirements.forEach { requirement -> requirement.consume(environment) }
		} catch (e: Throwable) {
			IonServer.slF4JLogger.error("There was an error executing multiblock recipe $key: ${e.message}")
			e.printStackTrace()
			return false
		}

		// Once ingredients have been sucessfully consumed, execute the result
		val executionResult = resultEnvironment.executeResult()
		itemResult?.executeCallbacks(environment, executionResult)
		environment.multiblock.tickingManager.sleepForTicks(resultSleepTicks)
		return true
	}
}
