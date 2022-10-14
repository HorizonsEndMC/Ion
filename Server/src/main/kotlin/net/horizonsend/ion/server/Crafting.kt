package net.horizonsend.ion.server

import net.horizonsend.ion.server.utilities.enumSetOf
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

// Special Exception Wildcard Imports
import org.bukkit.Material.*

val forbiddenCraftingItems = enumSetOf(
	WARPED_FUNGUS_ON_A_STICK, NETHERITE_PICKAXE, NETHERITE_SHOVEL, NETHERITE_SWORD, NETHERITE_AXE, NETHERITE_HOE
)

fun initializeCrafting() {
	// Prismarine Bricks
	furnaceRecipe("prismarine_bricks", PRISMARINE_BRICKS, PRISMARINE, 1f, 200)

	// Bell
	shapedRecipe("bell", BELL, "sos", "igi", "ggg") {
		setIngredient('g', GOLD_BLOCK)
		setIngredient('i', IRON_BLOCK)
		setIngredient('o', OAK_LOG)
		setIngredient('s', STICK)
	}

	// Enderpearl
	shapedRecipe("enderpearl", ENDER_PEARL, "oeo", "ede", "oeo") {
		setIngredient('d', DIAMOND_BLOCK)
		setIngredient('o', OBSIDIAN)
		setIngredient('e', EMERALD)
	}

	// Gunpowder
	shapelessRecipe(
		"gunpowder",
		GUNPOWDER,
		arrayOf(REDSTONE, FLINT, SAND, CHARCOAL)
	)

	// Wool -> String
	val wool = arrayOf(
		LIGHT_BLUE_WOOL, LIGHT_GRAY_WOOL, MAGENTA_WOOL, ORANGE_WOOL, PURPLE_WOOL, YELLOW_WOOL, BLACK_WOOL, BROWN_WOOL,
		GREEN_WOOL, WHITE_WOOL, BLUE_WOOL, CYAN_WOOL, GRAY_WOOL, LIME_WOOL, PINK_WOOL, RED_WOOL
	)

	for (material in wool) {
		shapelessRecipe(material.name.lowercase(), ItemStack(STRING, 4), arrayOf(material))
	}

	// Saddle
	shapedRecipe("saddle", SADDLE, "lll", "tat") {
		setIngredient('l', LEATHER)
		setIngredient('t', TRIPWIRE)
		setIngredient('a', AIR)
	}

	// Black Dye
	shapelessRecipe("black_dye", BLACK_DYE, arrayOf(COAL))

	// Prismarine Crystals
	shapelessRecipe("prismarine_crystals", ItemStack(PRISMARINE_CRYSTALS, 4), arrayOf(SEA_LANTERN))

	// Remove Unwanted Vanilla Recipes
	for (material in forbiddenCraftingItems) {
		for (recipe in Bukkit.getRecipesFor(ItemStack(material))) {
			if (recipe is Keyed) Bukkit.removeRecipe(recipe.key)
		}
	}
}

private fun furnaceRecipe(name: String, result: Material, source: Material, experience: Float, cookingTime: Int) {
	Bukkit.addRecipe(FurnaceRecipe(NamespacedKey(plugin, name), ItemStack(result), source, experience, cookingTime))
}

private fun shapedRecipe(name: String, result: Material, vararg shape: String, execute: ShapedRecipe.() -> Unit) {
	val recipe = ShapedRecipe(NamespacedKey(plugin, name), ItemStack(result))
	execute(recipe)
	recipe.shape(*shape)
	Bukkit.addRecipe(recipe)
}

private fun shapelessRecipe(name: String, result: ItemStack, ingredients: Array<Material>) {
	val recipe = ShapelessRecipe(NamespacedKey(plugin, name), result)
	for (ingredient in ingredients) recipe.addIngredient(ingredient)
	Bukkit.addRecipe(recipe)
}

private fun shapelessRecipe(name: String, result: Material, ingredients: Array<Material>) {
	shapelessRecipe(name, ItemStack(result), ingredients)
}