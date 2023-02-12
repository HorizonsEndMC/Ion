package net.horizonsend.ion.server.features.qol

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.customItems.CustomItems
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

@CommandAlias("recipe")
@Suppress("Unused")
@CommandPermission("ion.recipe")
class RecipeCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	@CommandPermission("ion.recipe")
	@CommandCompletion("@customItemAll")
	fun onCustomItemCommand(
		sender: Player,
		customItem: String
	) {
		val itemStack = CustomItems.getByIdentifier(customItem) ?: return
		val recipe = Bukkit.getRecipe(NamespacedKey(Ion, itemStack.identifier.lowercase()))

		if (recipe is ShapedRecipe) {
			shapedRecipe(sender, recipe)
		} else if (recipe is ShapelessRecipe) {
			shapelessRecipe(sender, recipe)
		}
	}

	fun shapedRecipe(sender: Player, recipe: ShapedRecipe) {
		val view: Inventory = Bukkit.createInventory(null, InventoryType.CRAFTING, "Recipe")
		val recipeShape: Array<String> = recipe.shape
		val ingredientMap: Map<Char, ItemStack> = recipe.ingredientMap
		for (j in recipeShape.indices) {
			for (k in 0 until recipeShape[j].length) {
				val item: ItemStack = ingredientMap[recipeShape[j].toCharArray()[k]] ?: continue
				view.setItem(j * 3 + k + 1, item)
			}
		}

		sender.openInventory(view)
	}

	fun shapelessRecipe(sender: Player, recipe: ShapelessRecipe) {
		val ingredients: List<ItemStack> = recipe.ingredientList
		val view: Inventory = Bukkit.createInventory(null, InventoryType.CRAFTING, "Recipe")
		for (i in ingredients.indices) {
			view.setItem(i + 1, ingredients[i])
		}

		sender.openInventory(view)
	}
}
