package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.AutoMasonRecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.CenterBlockRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultExecutionEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.AutoMasonMultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

class AutoMasonRecipe(
	key: IonRegistryKey<MultiblockRecipe<*>, AutoMasonRecipe>,
	inputItem: ItemRequirement,
	centerCheck: (Material?) -> Boolean,
	power: PowerRequirement<AutoMasonRecipeEnviornment>,
	val result: ResultHolder<AutoMasonRecipeEnviornment, ItemResult<AutoMasonRecipeEnviornment>>
) : MultiblockRecipe<AutoMasonRecipeEnviornment>(key, AutoMasonMultiblockEntity::class) {
	override val requirements: Collection<RequirementHolder<AutoMasonRecipeEnviornment, *, *>> = listOf(
		// Input item
		RequirementHolder.anySlot(
			requirement = inputItem,
		),
		// Power requirement
		RequirementHolder.simpleConsumable(
			{ it.powerStorage.getPower() },
			power
		),
		// Center Block requirement
		@Suppress("UNCHECKED_CAST")
		RequirementHolder(
			dataTypeClass = Material::class.java as Class<Material?>,
			getter = { it.getCenterBlock()?.getTypeSafe() },
			requirement = CenterBlockRequirement(centerCheck),
			failureStatus = Component.text("Mismatched center block!", NamedTextColor.RED)
		)
	)

	fun consumeIngredients(enviornment: AutoMasonRecipeEnviornment) {
		if (!verifyAllRequirements(enviornment, false)) return

		try {
			requirements.forEach { requirement -> requirement.consume(enviornment) }
		} catch (e: Throwable) {
			IonServer.slF4JLogger.error("There was an error executing multiblock recipe ${this@AutoMasonRecipe.key}: ${e.message}")
			e.printStackTrace()
			return
		}
	}

	override fun assemble(enviornment: AutoMasonRecipeEnviornment): Boolean {
		if (!verifyAllRequirements(enviornment, true)) return false
		if (!result.verifySpace(enviornment)) return false

		val resultEnviornment = ResultExecutionEnviornment(enviornment, this)

		result.buildTransaction(enviornment, resultEnviornment)

		try {
			resultEnviornment.requirements.forEach { requirement -> requirement.consume(enviornment) }
		} catch (e: Throwable) {
			IonServer.slF4JLogger.error("There was an error executing multiblock recipe ${this@AutoMasonRecipe.key}: ${e.message}")
			e.printStackTrace()
			return false
		}

		// Once ingredients have been sucessfully consumed, execute the result
		val executionResult = resultEnviornment.executeResult()
		result.executeCallbacks(enviornment, executionResult)
		return true
	}
}
