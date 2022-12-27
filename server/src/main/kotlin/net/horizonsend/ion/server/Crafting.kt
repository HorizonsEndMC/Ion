package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.items.CustomItems
import net.starlegacy.feature.misc.CustomItems as LegacyCustomItems
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.AIR
import org.bukkit.Material.BELL
import org.bukkit.Material.BLACK_WOOL
import org.bukkit.Material.BLUE_WOOL
import org.bukkit.Material.BROWN_WOOL
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.CYAN_WOOL
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GOLD_INGOT
import org.bukkit.Material.GRAY_WOOL
import org.bukkit.Material.GREEN_DYE
import org.bukkit.Material.GREEN_WOOL
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.LEATHER
import org.bukkit.Material.LIGHT_BLUE_WOOL
import org.bukkit.Material.LIGHT_GRAY_WOOL
import org.bukkit.Material.LIME_WOOL
import org.bukkit.Material.MAGENTA_WOOL
import org.bukkit.Material.NETHER_WART
import org.bukkit.Material.NETHER_WART_BLOCK
import org.bukkit.Material.OAK_LOG
import org.bukkit.Material.ORANGE_WOOL
import org.bukkit.Material.PINK_WOOL
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.PRISMARINE_BRICKS
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.PURPLE_WOOL
import org.bukkit.Material.QUARTZ
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.RED_WOOL
import org.bukkit.Material.SADDLE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.STICK
import org.bukkit.Material.STRING
import org.bukkit.Material.TRIPWIRE
import org.bukkit.Material.WHITE_WOOL
import org.bukkit.Material.YELLOW_WOOL
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

fun initializeCrafting() {
	// Prismarine Bricks
	Bukkit.addRecipe(FurnaceRecipe(NamespacedKey(Ion, "prismarine_bricks"), ItemStack(PRISMARINE_BRICKS), PRISMARINE, 1f, 200))

	// Bell
	shapedRecipe("bell", BELL) {
		shape("sos", "igi", "ggg")

		setIngredient('g', GOLD_BLOCK)
		setIngredient('i', IRON_BLOCK)
		setIngredient('o', OAK_LOG)
		setIngredient('s', STICK)
	}

	// Wool -> String
	val wool = arrayOf(
		LIGHT_BLUE_WOOL, LIGHT_GRAY_WOOL, MAGENTA_WOOL, ORANGE_WOOL, PURPLE_WOOL, YELLOW_WOOL, BLACK_WOOL, BROWN_WOOL,
		GREEN_WOOL, WHITE_WOOL, BLUE_WOOL, CYAN_WOOL, GRAY_WOOL, LIME_WOOL, PINK_WOOL, RED_WOOL
	)

	for (material in wool) {
		shapelessRecipe(material.name.lowercase(), ItemStack(STRING, 4), arrayOf(material))
	}

	// Saddle
	shapedRecipe("saddle", SADDLE) {
		shape("lll", "tat")

		setIngredient('l', LEATHER)
		setIngredient('t', TRIPWIRE)
		setIngredient('a', AIR)
	}

	// Prismarine Crystals
	shapelessRecipe("prismarine_crystals", ItemStack(PRISMARINE_CRYSTALS, 4), arrayOf(SEA_LANTERN))

	// Nether Wart Block -> Nether Warts
	shapelessRecipe("nether_warts", ItemStack(NETHER_WART, 9), arrayOf(NETHER_WART_BLOCK))

	// Blaster Barrel Crafting
	itemStackShapeRecipe("blaster_barrel", CustomItems.GUN_BARREL.constructItemStack()) {
		shape("tct", "ppp", "tct")

		setIngredient('t', RecipeChoice.ExactChoice(LegacyCustomItems.MINERAL_TITANIUM.singleItem()))
		setIngredient('c', COPPER_INGOT)
		setIngredient('p', PRISMARINE_CRYSTALS)
	}

	// Circuitry Crafting 1
	itemStackShapeRecipe("circuitry_1", CustomItems.CIRCUITRY.constructItemStack()) {
		shape("qdq", "arg", "ccc")

		setIngredient('a', RecipeChoice.ExactChoice(LegacyCustomItems.MINERAL_ALUMINUM.singleItem()))
		setIngredient('c', COPPER_INGOT)
		setIngredient('q', QUARTZ)
		setIngredient('g', GOLD_INGOT)
		setIngredient('d', GREEN_DYE)
		setIngredient('r', REDSTONE)
	}

	// Circuitry Crafting 2
	itemStackShapeRecipe("circuitry_2", CustomItems.CIRCUITRY.constructItemStack()) {
		shape("qdq", "gra", "ccc")

		setIngredient('a', RecipeChoice.ExactChoice(LegacyCustomItems.MINERAL_ALUMINUM.singleItem()))
		setIngredient('c', COPPER_INGOT)
		setIngredient('q', QUARTZ)
		setIngredient('g', GOLD_INGOT)
		setIngredient('d', GREEN_DYE)
		setIngredient('r', REDSTONE)
	}

	// Standard Magazine Crafting
	itemStackShapeRecipe("standard_magazine", CustomItems.MAGAZINE.constructItemStack()) {
		shape("aaa", "qqq", "aaa")

		setIngredient('a', RecipeChoice.ExactChoice(LegacyCustomItems.MINERAL_ALUMINUM.singleItem()))
		setIngredient('q', LAPIS_BLOCK)
	}
}

private fun shapedRecipe(name: String, result: Material, execute: ShapedRecipe.() -> Unit) {
	val recipe = ShapedRecipe(NamespacedKey(Ion, name), ItemStack(result))
	execute(recipe)
	Bukkit.addRecipe(recipe)
}

private fun itemStackShapeRecipe(name: String, result: ItemStack, execute: ShapedRecipe.() -> Unit){
	val recipe = ShapedRecipe(NamespacedKey(Ion, name), result)
	execute(recipe)
	Bukkit.addRecipe(recipe)
}

private fun shapelessRecipe(name: String, result: ItemStack, ingredients: Array<Material>) {
	val recipe = ShapelessRecipe(NamespacedKey(Ion, name), result)
	for (ingredient in ingredients) recipe.addIngredient(ingredient)
	Bukkit.addRecipe(recipe)
}
