package net.horizonsend.ion.server.features.starship.factory.integration

import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.multiblock.crafting.MultiblockRecipeRegistry
import net.horizonsend.ion.server.features.multiblock.crafting.input.AutoMasonRecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.AutoMasonRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock.InventoryReference
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.AutoMasonMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.starship.factory.AvailableItemInformation
import net.horizonsend.ion.server.features.starship.factory.PrintItem
import net.horizonsend.ion.server.features.starship.factory.ShipFactoryPrintTask
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.minecraft.world.inventory.ResultContainer
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftInventory

class AutoStonecutterIntegration(
	taskEntity: ShipFactoryEntity,
	entity: AutoMasonMultiblockEntity
) : ShipFactoryIntegration<AutoMasonMultiblockEntity>(taskEntity, entity) {
	private var recipeEnviornment: AutoMasonRecipeEnviornment? = null
	private val recipes = multimapOf<Material, AutoMasonRecipe>()

	private var availableItems: Map<PrintItem, AvailableItemInformation> = mapOf()

	override fun syncSetup(task: ShipFactoryPrintTask) {
		recipeEnviornment = buildRecipeEnviornment()
		recipeEnviornment?.wildcard = true

		availableItems = ShipFactoryPrintTask.getAvailableItems(
			setOfNotNull(integratedEntity.getInputInventory()?.let(InventoryReference::wrap)),
			taskEntity.settings
		)
	}

	override fun asyncSetup(task: ShipFactoryPrintTask) {
		val usedMaterials = task.blockMap.values.mapTo(mutableSetOf()) { it.material }

		for (recipe in MultiblockRecipeRegistry.getRecipes()) {
			if (recipe !is AutoMasonRecipe) continue
			val resultType = recipe.result.result.asItem().type

			if (!usedMaterials.contains(resultType)) continue

			recipes[resultType].add(recipe)
		}
	}

	override fun sendReport(task: ShipFactoryPrintTask, hasFinished: Boolean) {}

	private var transaction: MutableMap<Material, MutableMap<BlockKey, Int>>? = null

	override fun startNewTransaction(task: ShipFactoryPrintTask) {
		transaction = mutableMapOf()
	}

	override fun commitTransaction(task: ShipFactoryPrintTask): List<BlockKey> {
		val transaction = this.transaction ?: return listOf()

		val fails = mutableListOf<BlockKey>()

		val recipeEnviornment = recipeEnviornment ?: return transaction.flatMap { it.value.keys }

		for ((material, positions) in transaction) {
			val recipesForMaterial = recipes[material]

			if (recipesForMaterial.isEmpty()) {
				fails.addAll(positions.keys)
				continue
			}

			val recipe = recipesForMaterial.firstOrNull { it.verifyAllRequirements(recipeEnviornment) }
			if (recipe == null) {
				fails.addAll(positions.keys)
				continue
			}

			val resultCount = recipe.result.result.asItem().amount

			for ((_, count) in positions) {
				val executionCount = resultCount / count
				repeat(executionCount) { recipe.consumeIngredients(recipeEnviornment) }
			}
		}

		return fails
	}

	override fun canAddTransaction(printItem: PrintItem, printPosition: BlockKey, requiredAmount: Int): Boolean {
		val transaction = this.transaction ?: return false

		val enviornment = recipeEnviornment ?: return false
		val itemStack = kotlin.runCatching { fromItemString(printItem.itemString) }.getOrNull() ?: return false
		val recipes = recipes[itemStack.type]

		val recipe = recipes.firstOrNull { it.verifyAllRequirements(enviornment) }
		if (recipe == null) return false

		val ingredientMatches = recipe.getItemRequirements().mapNotNull { (it.requirement as ItemRequirement).asItemStack() }
		if (!ingredientMatches.all { checkAndDecrementResources(PrintItem(it), it.amount) }) return false

		transaction.getOrPut(itemStack.type) { mutableMapOf() }[printPosition] = requiredAmount

		return true
	}

	private fun buildRecipeEnviornment(): AutoMasonRecipeEnviornment? {
		val noOpInventory = CraftInventory(ResultContainer())

		return AutoMasonRecipeEnviornment(
			multiblock = integratedEntity,
			inputInventory = integratedEntity.getInputInventory() ?: return null,
			outputInventory = noOpInventory,
			powerStorage = integratedEntity.powerStorage,
			tickingManager = integratedEntity.tickingManager
		)
	}

	private fun checkAndDecrementResources(printItem: PrintItem, requiredAmount: Int): Boolean {
		val resourceInformation = availableItems[printItem] ?: return false
		if (resourceInformation.amount.get() < requiredAmount) return false

		resourceInformation.amount.addAndGet(-requiredAmount)

		return true
	}
}
