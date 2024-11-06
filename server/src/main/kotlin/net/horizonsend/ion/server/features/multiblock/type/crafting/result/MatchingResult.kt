package net.horizonsend.ion.server.features.multiblock.type.crafting.result

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient.MatchingIngredient
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.ProcessingMultiblockRecipe
import net.horizonsend.ion.server.miscellaneous.utils.getMatchingMaterials
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

open class MatchingResult(
	private val match: String,
	private val replaceStr : String,
	val ignore : Set<Material>,
	private val count: Int = 1) : MultiblockRecipeResult {
	val matches = run {
		val list: MutableSet<Material> = getMatchingMaterials { it.name.matches(Regex(match)) }
		list.removeAll(ignore)
		list.toSet()
	}

	override fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean {
		val inputIngredient = when (recipe) {
			is FurnaceMultiblockRecipe<*> -> (recipe.smelting as MatchingIngredient)
			is ProcessingMultiblockRecipe<*>-> (recipe.smelting as MatchingIngredient)
			else -> return false
		}
		val validInput : Set<Material>
		when (craftingInventory) {
			is CraftingInventory -> {
				validInput = craftingInventory.storageContents.mapNotNull { it?.type }.union(inputIngredient.matches)
				if (validInput.isEmpty()) return false
			}
			is FurnaceInventory -> {
				validInput = craftingInventory.storageContents.mapNotNull { it?.type }.union(inputIngredient.matches)
				if (validInput.isEmpty()) return false
			}
			else -> return false
		}

		val inputMat = validInput.first()
		val resultMat = matches.first { it.name == inputMat.name.replace(Regex(inputIngredient.match),replaceStr) }
		val result = ItemStack(resultMat)

		return when (craftingInventory) {
			is CraftingInventory -> {
				((craftingInventory.result?.isSimilar(result) ?: true)
					&& ((craftingInventory.result?.amount ?: 0) < result.maxStackSize))
			}

			is FurnaceInventory -> {
				((craftingInventory.result?.isSimilar(result) ?: true)
					&& ((craftingInventory.result?.amount ?: 0) < result.maxStackSize))
			}
			else -> false
		}
	}

	override fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign) {

		val inputIngredient : MatchingIngredient
		when (recipe) {
			is FurnaceMultiblockRecipe<*> -> inputIngredient = (recipe.smelting as MatchingIngredient)
			is ProcessingMultiblockRecipe<*>-> inputIngredient = (recipe.smelting as MatchingIngredient)
			else -> return
		}
		val validInput : Set<Material>
		when (craftingInventory) {
			is CraftingInventory -> {
				validInput = craftingInventory.storageContents.mapNotNull { it?.type }.union(inputIngredient.matches)
				if (validInput.isEmpty()) return
			}
			is FurnaceInventory -> {
				validInput = craftingInventory.storageContents.mapNotNull { it?.type }.union(inputIngredient.matches)
				if (validInput.isEmpty()) return
			}
			else -> return
		}
		val inputMat = validInput.first()
		val resultMat = matches.first { it.name == inputMat.name.replace(Regex(inputIngredient.match),replaceStr) }
		val result = ItemStack(resultMat)

		applyResultTo(craftingInventory) { result.asQuantity(count) }
	}

	private fun applyResultTo(inventory: Inventory, result: Supplier<ItemStack>) {
		when (inventory) {
			is CraftingInventory -> inventory.result?.add(1) ?: run { inventory.result = result.get() }
			is FurnaceInventory -> inventory.result?.add(1) ?: run { inventory.result = result.get() }
		}
	}
}
