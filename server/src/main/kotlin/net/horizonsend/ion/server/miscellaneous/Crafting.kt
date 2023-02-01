package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.customItems.CustomItems
import net.horizonsend.ion.server.features.customItems.CustomItems.CIRCUITRY
import net.horizonsend.ion.server.features.customItems.CustomItems.GUN_BARREL
import net.horizonsend.ion.server.features.customItems.CustomItems.PISTOL
import net.horizonsend.ion.server.features.customItems.CustomItems.PISTOL_RECEIVER
import net.horizonsend.ion.server.features.customItems.CustomItems.RIFLE
import net.horizonsend.ion.server.features.customItems.CustomItems.RIFLE_RECEIVER
import net.horizonsend.ion.server.features.customItems.CustomItems.SHOTGUN_RECEIVER
import net.horizonsend.ion.server.features.customItems.CustomItems.SMB_RECEIVER
import net.horizonsend.ion.server.features.customItems.CustomItems.SNIPER_RECEIVER
import net.horizonsend.ion.server.features.customItems.CustomItems.STANDARD_MAGAZINE
import net.horizonsend.ion.server.features.customItems.CustomItems.SUBMACHINE_BLASTER
import net.starlegacy.feature.misc.CustomItems.MINERAL_ALUMINUM
import net.starlegacy.feature.misc.CustomItems.MINERAL_TITANIUM
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.AIR
import org.bukkit.Material.AMETHYST_SHARD
import org.bukkit.Material.BELL
import org.bukkit.Material.BLACK_WOOL
import org.bukkit.Material.BLUE_WOOL
import org.bukkit.Material.BROWN_WOOL
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.CYAN_WOOL
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.GLASS
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GOLD_INGOT
import org.bukkit.Material.GRAY_WOOL
import org.bukkit.Material.GREEN_DYE
import org.bukkit.Material.GREEN_WOOL
import org.bukkit.Material.HONEYCOMB
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.LEATHER
import org.bukkit.Material.LIGHT_BLUE_WOOL
import org.bukkit.Material.LIGHT_GRAY_WOOL
import org.bukkit.Material.LIME_WOOL
import org.bukkit.Material.MAGENTA_WOOL
import org.bukkit.Material.NETHER_WART
import org.bukkit.Material.NETHER_WART_BLOCK
import org.bukkit.Material.OAK_LOG
import org.bukkit.Material.OCHRE_FROGLIGHT
import org.bukkit.Material.ORANGE_WOOL
import org.bukkit.Material.PEARLESCENT_FROGLIGHT
import org.bukkit.Material.PINK_WOOL
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.PRISMARINE_BRICKS
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.PURPLE_WOOL
import org.bukkit.Material.QUARTZ
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.RED_WOOL
import org.bukkit.Material.SADDLE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SHROOMLIGHT
import org.bukkit.Material.SLIME_BALL
import org.bukkit.Material.STICK
import org.bukkit.Material.STRING
import org.bukkit.Material.TRIPWIRE
import org.bukkit.Material.VERDANT_FROGLIGHT
import org.bukkit.Material.WHITE_WOOL
import org.bukkit.Material.YELLOW_WOOL
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice.ExactChoice
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

	// Ochre Froglight
	shapedRecipe("ochre_froglight", OCHRE_FROGLIGHT) {
		shape("xhx", "hlh", "xhx")

		setIngredient('h', HONEYCOMB)
		setIngredient('l', SHROOMLIGHT)
		setIngredient('x', AIR)
	}

	// Verdant Froglight
	shapedRecipe("verdant_froglight", VERDANT_FROGLIGHT) {
		shape("xsx", "sls", "xsx")

		setIngredient('s', SLIME_BALL)
		setIngredient('l', SHROOMLIGHT)
		setIngredient('x', AIR)
	}

	// Pearlescent Froglight
	shapedRecipe("pearlescent_froglight", PEARLESCENT_FROGLIGHT) {
		shape("xax", "ala", "xax")

		setIngredient('a', AMETHYST_SHARD)
		setIngredient('l', SHROOMLIGHT)
		setIngredient('x', AIR)
	}

	// Prismarine Crystals
	shapelessRecipe("prismarine_crystals", ItemStack(PRISMARINE_CRYSTALS, 4), arrayOf(SEA_LANTERN))

	// Nether Wart Block -> Nether Warts
	shapelessRecipe("nether_warts", ItemStack(NETHER_WART, 9), arrayOf(NETHER_WART_BLOCK))

	// Blaster Barrel Crafting
	itemStackShapeRecipe("blaster_barrel", GUN_BARREL.constructItemStack()) {
		shape("tct", "ppp", "tct")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('c', COPPER_INGOT)
		setIngredient('p', PRISMARINE_CRYSTALS)
	}

	// Pistol Receiver Crafting
	itemStackShapeRecipe("pistol_receiver", PISTOL_RECEIVER.constructItemStack()) {
		shape("xxx", "irt", "xxx")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('r', REDSTONE_BLOCK)
		setIngredient('i', IRON_TRAPDOOR)
		setIngredient('x', AIR)
	}

	// Rifle Receiver Crafting
	itemStackShapeRecipe("rifle_receiver", RIFLE_RECEIVER.constructItemStack()) {
		shape("xtx", "igt", "xtx")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('g', GOLD_BLOCK)
		setIngredient('i', IRON_TRAPDOOR)
		setIngredient('x', AIR)
	}

	// SMB Receiver Crafting
	itemStackShapeRecipe("smb_receiver", SMB_RECEIVER.constructItemStack()) {
		shape("xtx", "idx", "xtx")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('d', DIAMOND_BLOCK)
		setIngredient('i', IRON_TRAPDOOR)
		setIngredient('x', AIR)
	}

	// Sniper Receiver Crafting
	itemStackShapeRecipe("sniper_receiver", SNIPER_RECEIVER.constructItemStack()) {
		shape("xtx", "ieb", "xtx")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('e', EMERALD_BLOCK)
		setIngredient('b', ExactChoice(MINERAL_TITANIUM.fullBlock.singleItem()))
		setIngredient('i', IRON_TRAPDOOR)
		setIngredient('x', AIR)
	}

	// Shotgun Receiver Crafting
	itemStackShapeRecipe("shotgun_receiver", SHOTGUN_RECEIVER.constructItemStack()) {
		shape("xxx", "icb", "xtx")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('c', COPPER_BLOCK)
		setIngredient('b', ExactChoice(MINERAL_TITANIUM.fullBlock.singleItem()))
		setIngredient('i', IRON_TRAPDOOR)
		setIngredient('x', AIR)
	}

	// Pistol Crafting
	itemStackShapeRecipe("pistol", PISTOL.constructItemStack()) {
		shape("xxx", "apb", "cxx")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('p', PISTOL_RECEIVER.constructItemStack())
		setIngredient('b', GUN_BARREL.constructItemStack())
		setIngredient('c', CIRCUITRY.constructItemStack())
		setIngredient('x', AIR)
	}

	// Rifle Crafting
	itemStackShapeRecipe("rifle", RIFLE.constructItemStack()) {
		shape("xxx", "apb", "acx")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('p', RIFLE_RECEIVER.constructItemStack())
		setIngredient('b', GUN_BARREL.constructItemStack())
		setIngredient('c', CIRCUITRY.constructItemStack())
		setIngredient('x', AIR)
	}

	// SMB Crafting
	itemStackShapeRecipe("submachine_blaster", SUBMACHINE_BLASTER.constructItemStack()) {
		shape("xxx", "apb", "acx")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('p', SMB_RECEIVER.constructItemStack())
		setIngredient('b', GUN_BARREL.constructItemStack())
		setIngredient('c', CIRCUITRY.constructItemStack())
		setIngredient('x', AIR)
	}

	// Sniper Crafting
	itemStackShapeRecipe("sniper", CustomItems.SNIPER.constructItemStack()) {
		shape("xgx", "apb", "acx")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('p', SNIPER_RECEIVER.constructItemStack())
		setIngredient('b', GUN_BARREL.constructItemStack())
		setIngredient('c', CIRCUITRY.constructItemStack())
		setIngredient('g', GLASS)
		setIngredient('x', AIR)
	}

	// Shotgun Crafting
	itemStackShapeRecipe("shotgun", CustomItems.SHOTGUN.constructItemStack()) {
		shape("xxb", "apb", "acx")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('p', SHOTGUN_RECEIVER.constructItemStack())
		setIngredient('b', GUN_BARREL.constructItemStack())
		setIngredient('c', CIRCUITRY.constructItemStack())
		setIngredient('x', AIR)
	}

	// Circuitry Crafting 1
	itemStackShapeRecipe("circuitry_1", CIRCUITRY.constructItemStack()) {
		shape("qdq", "arg", "ccc")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('c', COPPER_INGOT)
		setIngredient('q', QUARTZ)
		setIngredient('g', GOLD_INGOT)
		setIngredient('d', GREEN_DYE)
		setIngredient('r', REDSTONE)
	}

	// Circuitry Crafting 2
	itemStackShapeRecipe("circuitry_2", CIRCUITRY.constructItemStack()) {
		shape("qdq", "gra", "ccc")

		setIngredient('a', ExactChoice(MINERAL_ALUMINUM.singleItem()))
		setIngredient('c', COPPER_INGOT)
		setIngredient('q', QUARTZ)
		setIngredient('g', GOLD_INGOT)
		setIngredient('d', GREEN_DYE)
		setIngredient('r', REDSTONE)
	}

	// Standard Magazine Crafting
	itemStackShapeRecipe("standard_magazine", STANDARD_MAGAZINE.constructItemStack()) {
		shape("xxx", "rlr", "ttt")

		setIngredient('t', ExactChoice(MINERAL_TITANIUM.singleItem()))
		setIngredient('l', LAPIS_BLOCK)
		setIngredient('r', REDSTONE)
		setIngredient('x', AIR)
	}
}

private fun shapedRecipe(name: String, result: Material, execute: ShapedRecipe.() -> Unit) {
	val recipe = ShapedRecipe(NamespacedKey(Ion, name), ItemStack(result))
	execute(recipe)
	Bukkit.addRecipe(recipe)
}

private fun itemStackShapeRecipe(name: String, result: ItemStack, execute: ShapedRecipe.() -> Unit) {
	val recipe = ShapedRecipe(NamespacedKey(Ion, name), result)
	execute(recipe)
	Bukkit.addRecipe(recipe)
}

private fun shapelessRecipe(name: String, result: ItemStack, ingredients: Array<Material>) {
	val recipe = ShapelessRecipe(NamespacedKey(Ion, name), result)
	for (ingredient in ingredients) recipe.addIngredient(ingredient)
	Bukkit.addRecipe(recipe)
}
