package net.horizonsend.ion.server

import net.horizonsend.ion.server.utilities.enumSetOf
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

val forbiddenCraftingItems = enumSetOf(
	Material.WARPED_FUNGUS_ON_A_STICK, Material.NETHERITE_AXE, Material.NETHERITE_HOE, Material.NETHERITE_SWORD,
	Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL
)

fun initializeCrafting() {
	/**
	 * Recipes
	 */
	// Prismarine Bricks
	Bukkit.addRecipe(
		FurnaceRecipe(
			NamespacedKey(plugin, "prismarine_bricks_recipe"),
			ItemStack(Material.PRISMARINE_BRICKS),
			Material.PRISMARINE,
			1f,
			200
		)
	)

	// Bell
	Bukkit.addRecipe(ShapedRecipe(NamespacedKey(plugin, "bell_recipe"), ItemStack(Material.BELL)).apply {
		shape("wow", "szs", "zzz")
		setIngredient('w', RecipeChoice.MaterialChoice(Material.STICK))
		setIngredient('o', RecipeChoice.MaterialChoice(Material.OAK_LOG))
		setIngredient('s', RecipeChoice.MaterialChoice(Material.IRON_BLOCK))
		setIngredient('z', RecipeChoice.MaterialChoice(Material.GOLD_BLOCK))
	})

	// Enderpearl
	Bukkit.addRecipe(ShapedRecipe(NamespacedKey(plugin, "enderpearl_recipe"), ItemStack(Material.ENDER_PEARL)).apply {
		shape("wow", "oso", "wow")
		setIngredient('w', RecipeChoice.MaterialChoice(Material.OBSIDIAN))
		setIngredient('o', RecipeChoice.MaterialChoice(Material.EMERALD))
		setIngredient('s', RecipeChoice.MaterialChoice(Material.DIAMOND_BLOCK))
	})

	// Gunpowder
	Bukkit.addRecipe(ShapelessRecipe(NamespacedKey(plugin, "gunpowder_recipe"), ItemStack(Material.GUNPOWDER)).apply {
		addIngredient(Material.REDSTONE)
		addIngredient(Material.FLINT)
		addIngredient(Material.SAND)
		addIngredient(Material.CHARCOAL)
	})

	// Wool -> String
	arrayOf(
		Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
		Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
		Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL,
		Material.RED_WOOL, Material.BLACK_WOOL
	).forEach {
		Bukkit.addRecipe(
			ShapelessRecipe(
				NamespacedKey(plugin, "${it.name.lowercase()}_string_recipe"),
				ItemStack(Material.STRING, 4)
			).apply {
				addIngredient(1, it)
			}
		)
	}

	// Saddle
	Bukkit.addRecipe(ShapedRecipe(NamespacedKey(plugin, "Saddle_Recipe"), ItemStack(Material.SADDLE)).apply {
		shape("lll", "tat")
		setIngredient('l', Material.LEATHER)
		setIngredient('t', Material.TRIPWIRE)
		setIngredient('a', Material.AIR)
	})

	//black dye
	Bukkit.addRecipe(ShapelessRecipe(NamespacedKey(plugin, "Coal_Black_Dye_Recipe"), ItemStack(Material.BLACK_DYE)).apply {
		addIngredient(1, Material.COAL)
	})

	// Remove Unwanted Vanilla Recipes
	forbiddenCraftingItems.forEach { material ->
		Bukkit.getRecipesFor(ItemStack(material)).forEach {
			if (it is Keyed) Bukkit.removeRecipe(it.key)
		}
	}
}