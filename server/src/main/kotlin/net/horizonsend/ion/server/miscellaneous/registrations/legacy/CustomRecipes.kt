package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.ENDER_PEARL
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.END_STONE
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.WARPED_PLANKS
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object CustomRecipes : IonServerComponent() {
	override fun onEnable() {
		Tasks.syncDelay(1) {
			registerWireRecipe()
			registerSeaLanternRecipe()
			registerEndPortalFrameRecipe()
		}
	}

	private fun registerShapedRecipe(
		id: String,
		output: ItemStack,
		vararg shape: String,
		ingredients: Map<Char, RecipeChoice>
	): ShapedRecipe {
		val key = NamespacedKey(IonServer, id)

		val recipe = ShapedRecipe(key, output)

		recipe.shape(*shape)

		for ((char, ingredient: RecipeChoice) in ingredients) {
			recipe.setIngredient(char, ingredient)
		}

		addRecipe(recipe, key)

		return recipe
	}

	private fun registerShapelessRecipe(
		id: String,
		output: ItemStack,
		vararg ingredients: RecipeChoice
	): ShapelessRecipe {
		check(ingredients.isNotEmpty())

		val key = NamespacedKey(IonServer, id)

		val recipe = ShapelessRecipe(key, output)

		for (ingredient in ingredients) {
			recipe.addIngredient(ingredient)
		}

		addRecipe(recipe, key)

		return recipe
	}

	private fun addRecipe(recipe: Recipe, key: NamespacedKey, attempt: Int = 1) {
		Bukkit.getServer().addRecipe(recipe)
		if (attempt > 1) {
			val added = Bukkit.getServer().getRecipe(key) != null
			log.info("Recipe $key Added (attempt $attempt): ${(added)}")
		}
		Tasks.syncDelay(1) {
			val kept = Bukkit.getServer().getRecipe(key) != null
			if (attempt > 1) {
				log.info("Recipe $kept Kept (attempt $attempt): ${(kept)}")
			}
			if (!kept) {
				addRecipe(recipe, key, attempt + 1)
			}
		}
	}

	private fun materialChoice(material: Material): RecipeChoice {
		return RecipeChoice.MaterialChoice(material)
	}

	private fun registerWireRecipe() {
		registerShapedRecipe(
			"end_rod",
			ItemStack(END_ROD, 16), "ccc",
			ingredients = mapOf(
				'c' to materialChoice(COPPER_INGOT)
			)
		)
	}

	private fun registerSeaLanternRecipe() {
		registerShapelessRecipe(
			"sea_lantern",
			ItemStack(SEA_LANTERN, 1),
			materialChoice(PRISMARINE_CRYSTALS),
			materialChoice(PRISMARINE_CRYSTALS),
			materialChoice(PRISMARINE_CRYSTALS),
			materialChoice(PRISMARINE_CRYSTALS)
		)
	}

	private fun registerEndPortalFrameRecipe() {
		registerShapedRecipe(
			"end_portal_frame",
			ItemStack(END_PORTAL_FRAME, 1),
			"wow", "sss",
			ingredients = mapOf(
				'w' to materialChoice(WARPED_PLANKS),
				'o' to materialChoice(ENDER_PEARL),
				's' to materialChoice(END_STONE)
			)
		)
	}
}
