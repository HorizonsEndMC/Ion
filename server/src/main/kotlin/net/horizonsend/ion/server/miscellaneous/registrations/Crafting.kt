package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.ALUMINUM
import net.horizonsend.ion.server.features.customitems.CustomItems.ALUMINUM_BLOCK
import net.horizonsend.ion.server.features.customitems.CustomItems.CANNON_RECEIVER
import net.horizonsend.ion.server.features.customitems.CustomItems.CHETHERITE
import net.horizonsend.ion.server.features.customitems.CustomItems.CHETHERITE_BLOCK
import net.horizonsend.ion.server.features.customitems.CustomItems.CIRCUITRY
import net.horizonsend.ion.server.features.customitems.CustomItems.DETONATOR
import net.horizonsend.ion.server.features.customitems.CustomItems.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.customitems.CustomItems.GUN_BARREL
import net.horizonsend.ion.server.features.customitems.CustomItems.PISTOL
import net.horizonsend.ion.server.features.customitems.CustomItems.PISTOL_RECEIVER
import net.horizonsend.ion.server.features.customitems.CustomItems.RIFLE
import net.horizonsend.ion.server.features.customitems.CustomItems.RIFLE_RECEIVER
import net.horizonsend.ion.server.features.customitems.CustomItems.SHOTGUN_RECEIVER
import net.horizonsend.ion.server.features.customitems.CustomItems.SMB_RECEIVER
import net.horizonsend.ion.server.features.customitems.CustomItems.SNIPER_RECEIVER
import net.horizonsend.ion.server.features.customitems.CustomItems.SPECIAL_MAGAZINE
import net.horizonsend.ion.server.features.customitems.CustomItems.STANDARD_MAGAZINE
import net.horizonsend.ion.server.features.customitems.CustomItems.SUBMACHINE_BLASTER
import net.horizonsend.ion.server.features.customitems.CustomItems.TITANIUM
import net.horizonsend.ion.server.features.customitems.CustomItems.TITANIUM_BLOCK
import net.horizonsend.ion.server.features.customitems.CustomItems.URANIUM
import net.horizonsend.ion.server.features.customitems.CustomItems.URANIUM_BLOCK
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
import org.bukkit.Material.GLASS_PANE
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

object Crafting : IonServerComponent() {
	override fun onEnable() {
		// Prismarine Bricks
		Bukkit.addRecipe(
			FurnaceRecipe(
				NamespacedKey(IonServer, "prismarine_bricks"),
				ItemStack(PRISMARINE_BRICKS),
				PRISMARINE,
				1f,
				200
			)
		)

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
			LIGHT_BLUE_WOOL,
			LIGHT_GRAY_WOOL,
			MAGENTA_WOOL,
			ORANGE_WOOL,
			PURPLE_WOOL,
			YELLOW_WOOL,
			BLACK_WOOL,
			BROWN_WOOL,
			GREEN_WOOL,
			WHITE_WOOL,
			BLUE_WOOL,
			CYAN_WOOL,
			GRAY_WOOL,
			LIME_WOOL,
			PINK_WOOL,
			RED_WOOL
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

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('p', PRISMARINE_CRYSTALS)
		}

		// Pistol Receiver Crafting
		itemStackShapeRecipe("pistol_receiver", PISTOL_RECEIVER.constructItemStack()) {
			shape("xxx", "irt", "xxx")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('r', REDSTONE_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Rifle Receiver Crafting
		itemStackShapeRecipe("rifle_receiver", RIFLE_RECEIVER.constructItemStack()) {
			shape("xtx", "igt", "xtx")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('g', GOLD_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// SMB Receiver Crafting
		itemStackShapeRecipe("smb_receiver", SMB_RECEIVER.constructItemStack()) {
			shape("xtx", "idx", "xtx")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('d', DIAMOND_BLOCK)
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Sniper Receiver Crafting
		itemStackShapeRecipe("sniper_receiver", SNIPER_RECEIVER.constructItemStack()) {
			shape("xtx", "ieb", "xtx")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('b', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Shotgun Receiver Crafting
		itemStackShapeRecipe("shotgun_receiver", SHOTGUN_RECEIVER.constructItemStack()) {
			shape("xxx", "icb", "xtx")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('c', COPPER_BLOCK)
			setIngredient('b', ExactChoice(TITANIUM_BLOCK.constructItemStack()))
			setIngredient('i', IRON_TRAPDOOR)
			setIngredient('x', AIR)
		}

		// Cannon Receiver Crafting
		itemStackShapeRecipe("cannon_receiver", CANNON_RECEIVER.constructItemStack()) {
			shape("xxx", "xba", "gxx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('b', ExactChoice(ALUMINUM_BLOCK.constructItemStack()))
			setIngredient('g', GOLD_INGOT)
			setIngredient('x', AIR)
		}

		// Pistol Crafting
		itemStackShapeRecipe("pistol", PISTOL.constructItemStack()) {
			shape("xxx", "apb", "cxx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('p', PISTOL_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// Rifle Crafting
		itemStackShapeRecipe("rifle", RIFLE.constructItemStack()) {
			shape("xxx", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('p', RIFLE_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// SMB Crafting
		itemStackShapeRecipe("submachine_blaster", SUBMACHINE_BLASTER.constructItemStack()) {
			shape("xxx", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('p', SMB_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		// Sniper Crafting
		itemStackShapeRecipe("sniper", CustomItems.SNIPER.constructItemStack()) {
			shape("xgx", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('p', SNIPER_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('g', GLASS)
			setIngredient('x', AIR)
		}

		// Shotgun Crafting
		itemStackShapeRecipe("shotgun", CustomItems.SHOTGUN.constructItemStack()) {
			shape("xxb", "apb", "acx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('p', SHOTGUN_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
		}

		itemStackShapeRecipe("cannon", CustomItems.CANNON.constructItemStack()) {
			shape("xax", "xcb", "pxx")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('p', CANNON_RECEIVER.constructItemStack())
			setIngredient('b', GUN_BARREL.constructItemStack())
			setIngredient('c', CIRCUITRY.constructItemStack())
			setIngredient('x', AIR)
			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
		}

		// Circuitry Crafting 1
		itemStackShapeRecipe("circuitry_1", CIRCUITRY.constructItemStack()) {
			shape("qdq", "arg", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}

		// Circuitry Crafting 2
		itemStackShapeRecipe("circuitry_2", CIRCUITRY.constructItemStack()) {
			shape("qdq", "gra", "ccc")

			setIngredient('a', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('c', COPPER_INGOT)
			setIngredient('q', QUARTZ)
			setIngredient('g', GOLD_INGOT)
			setIngredient('d', GREEN_DYE)
			setIngredient('r', REDSTONE)
		}

		// Standard Magazine Crafting
		itemStackShapeRecipe("standard_magazine", STANDARD_MAGAZINE.constructItemStack()) {
			shape("xxx", "rlr", "ttt")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('l', LAPIS_BLOCK)
			setIngredient('r', REDSTONE)
			setIngredient('x', AIR)
		}

		// Special Magazine Crafting
		itemStackShapeRecipe("special_magazine", SPECIAL_MAGAZINE.constructItemStack()) {
			shape("xxx", "rer", "ttt")

			setIngredient('t', ExactChoice(TITANIUM.constructItemStack()))
			setIngredient('e', EMERALD_BLOCK)
			setIngredient('r', REDSTONE)
			setIngredient('x', AIR)
		}

		// Empty Gas Canister Crafting
		itemStackShapeRecipe("empty_gas_canister", GAS_CANISTER_EMPTY.constructItemStack()) {
			shape("xix", "igi", "xix")

			setIngredient('i', ExactChoice(ALUMINUM.constructItemStack()))
			setIngredient('g', GLASS_PANE)
			setIngredient('x', AIR)
		}

		// Detonator Crafting
		itemStackShapeRecipe("detonator", DETONATOR.constructItemStack()) {
			shape(" r ", "tut", " t ",)

			setIngredient('r', REDSTONE)
			setIngredient('t', TITANIUM.constructItemStack())
			setIngredient('u', URANIUM.constructItemStack())
		}

		// Aluminum Block Crafting
		itemStackShapelessRecipe("aluminumBlock", ALUMINUM_BLOCK.constructItemStack()) {
			addIngredient(ALUMINUM.constructItemStack().asQuantity(9))
		}

		// Aluminum Crafting
		itemStackShapelessRecipe("aluminum", ALUMINUM.constructItemStack().asQuantity(9)) {
			addIngredient(ALUMINUM_BLOCK.constructItemStack())
		}

		// Chetherite Block Crafting
		itemStackShapelessRecipe("chetheriteBlock", CHETHERITE_BLOCK.constructItemStack()) {
			addIngredient(CHETHERITE.constructItemStack().asQuantity(9))
		}

		// Chetherite Crafting
		itemStackShapelessRecipe("chetherite", CHETHERITE.constructItemStack().asQuantity(9)) {
			addIngredient(CHETHERITE_BLOCK.constructItemStack())
		}

		// Titanium Block Crafting
		itemStackShapelessRecipe("titaniumBlock", TITANIUM_BLOCK.constructItemStack()) {
			addIngredient(TITANIUM.constructItemStack().asQuantity(9))
		}

		// Titanium Crafting
		itemStackShapelessRecipe("titanium", TITANIUM.constructItemStack().asQuantity(9)) {
			addIngredient(TITANIUM_BLOCK.constructItemStack())
		}

		// Uranium Block Crafting
		itemStackShapelessRecipe("uraniumBlock", URANIUM_BLOCK.constructItemStack()) {
			addIngredient(URANIUM.constructItemStack().asQuantity(9))
		}

		// Uranium Crafting
		itemStackShapelessRecipe("uranium", URANIUM.constructItemStack().asQuantity(9)) {
			addIngredient(URANIUM_BLOCK.constructItemStack())
		}
	}

	private fun shapedRecipe(name: String, result: Material, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKey(IonServer, name), ItemStack(result))
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun itemStackShapeRecipe(name: String, result: ItemStack, execute: ShapedRecipe.() -> Unit) {
		val recipe = ShapedRecipe(NamespacedKey(IonServer, name), result)
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}

	private fun shapelessRecipe(name: String, result: ItemStack, ingredients: Array<Material>) {
		val recipe = ShapelessRecipe(NamespacedKey(IonServer, name), result)
		for (ingredient in ingredients) recipe.addIngredient(ingredient)
		Bukkit.addRecipe(recipe)
	}

	private fun itemStackShapelessRecipe(name: String, result: ItemStack, execute: ShapelessRecipe.() -> Unit) {
		val recipe = ShapelessRecipe(NamespacedKey(IonServer, name), result)
		execute(recipe)
		Bukkit.addRecipe(recipe)
	}
}
