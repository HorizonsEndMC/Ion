package net.horizonsend.ion.server.features.qol

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.customitems.CustomItems
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

@CommandAlias("recipe")
@Suppress("Unused")
@CommandPermission("ion.recipe")
class RecipeCommand : BaseCommand(), Listener {
	companion object {
		val invs = mutableListOf<InventoryView>()
	}

	@EventHandler
	fun onClick(ev: InventoryClickEvent) {
		if (invs.contains(ev.view)) {
			ev.isCancelled = true
		}
	}

	@Default
	@Suppress("Unused")
	@CommandPermission("ion.recipe")
	@CommandCompletion("@customItem")
	fun onCustomItemCommand(
		sender: Player,
		customItem: String
	) {
		val itemStack = CustomItems.getByIdentifier(customItem) ?: return
		val recipe = Bukkit.getRecipe(NamespacedKey(IonServer, itemStack.identifier.lowercase()))

		if (recipe is ShapedRecipe) {
			shapedRecipe(sender, recipe)
		} else if (recipe is ShapelessRecipe) {
			shapelessRecipe(sender, recipe)
		}
	}

	@Subcommand("legacy")
	@CommandPermission("ion.recipe")
	@CommandCompletion("@customitem")
	fun onItemCommand(
		sender: Player,
		customItem: String
	) {
		val itemStack = net.horizonsend.ion.server.features.misc.CustomItems[customItem] ?: return
		val recipe = Bukkit.getRecipe(NamespacedKey(IonServer, itemStack.id))

		if (recipe is ShapedRecipe) {
			shapedRecipe(sender, recipe)
		} else if (recipe is ShapelessRecipe) {
			shapelessRecipe(sender, recipe)
		}
	}

	fun shapedRecipe(sender: Player, recipe: ShapedRecipe) {
		val view: InventoryView = sender.openWorkbench(null, true)!!
		val recipeShape: Array<String> = recipe.shape
		val ingredientMap: Map<Char, ItemStack> =
			recipe.choiceMap.filter { it.value is MaterialChoice }.mapValues { (it.value as MaterialChoice).itemStack }

		for (j in recipeShape.indices) {
			for (k in 0 until recipeShape[j].length) {
				val item: ItemStack = ingredientMap[recipeShape[j].toCharArray()[k]] ?: continue
				view.setItem(j * 3 + k + 1, item)
			}
		}

		invs.add(view)
	}

	fun shapelessRecipe(sender: Player, recipe: ShapelessRecipe) {
		val ingredients: List<ItemStack> =
			recipe.choiceList.filterIsInstance<MaterialChoice>().map { it.itemStack }

		val view = sender.openWorkbench(null, true)!!
		for (i in ingredients.indices) {
			view.setItem(i + 1, ingredients[i])
		}

		invs.add(view)
	}
}
