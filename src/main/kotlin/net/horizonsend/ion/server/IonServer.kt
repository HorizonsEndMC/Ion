package net.horizonsend.ion.server

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Plugin entrypoint
class IonServer : JavaPlugin() {
	private val asyncInit = AsyncInit(this)

	override fun onEnable() {
		asyncInit.start()

		server.addRecipe(FurnaceRecipe(NamespacedKey(this, "prismarine_bricks_recipe"), ItemStack(Material.PRISMARINE_BRICKS), Material.PRISMARINE, 1f, 200))

		val bellRecipe = ShapedRecipe(NamespacedKey(this, "bell_recipe"), ItemStack(Material.BELL))
		bellRecipe.shape("wow", "szs", "zzz")
		bellRecipe.setIngredient('w', RecipeChoice.MaterialChoice(Material.STICK))
		bellRecipe.setIngredient('o', RecipeChoice.MaterialChoice(Material.OAK_LOG))
		bellRecipe.setIngredient('s', RecipeChoice.MaterialChoice(Material.IRON_BLOCK))
		bellRecipe.setIngredient('z', RecipeChoice.MaterialChoice(Material.GOLD_BLOCK))
		server.addRecipe(bellRecipe)

		val enderpearlRecipe = ShapedRecipe(NamespacedKey(this, "enderpearlRecipe"), ItemStack(Material.ENDER_PEARL))
		enderpearlRecipe.shape("wow", "oso", "wow")
		enderpearlRecipe.setIngredient('w', RecipeChoice.MaterialChoice(Material.OBSIDIAN))
		enderpearlRecipe.setIngredient('o', RecipeChoice.MaterialChoice(Material.EMERALD))
		enderpearlRecipe.setIngredient('s', RecipeChoice.MaterialChoice(Material.DIAMOND_BLOCK))
		server.addRecipe(enderpearlRecipe)

		arrayOf(
			Material.WHITE_WOOL,
			Material.ORANGE_WOOL,
			Material.MAGENTA_WOOL,
			Material.LIGHT_BLUE_WOOL,
			Material.YELLOW_WOOL,
			Material.LIME_WOOL,
			Material.PINK_WOOL,
			Material.GRAY_WOOL,
			Material.LIGHT_GRAY_WOOL,
			Material.CYAN_WOOL,
			Material.PURPLE_WOOL,
			Material.BLUE_WOOL,
			Material.BROWN_WOOL,
			Material.GREEN_WOOL,
			Material.RED_WOOL,
			Material.BLACK_WOOL
		).forEach {
			val woolType = ShapelessRecipe(
				NamespacedKey(this, "${it.name.lowercase()}_string_recipe"),
				ItemStack(Material.STRING, 4)
			)
			woolType.addIngredient(1, it)
			server.addRecipe(woolType)
		}

		val gunpowderRecipe = ShapelessRecipe(NamespacedKey(this, "gunpowder_recipe"), ItemStack(Material.GUNPOWDER))
		gunpowderRecipe.addIngredient(Material.REDSTONE)
		gunpowderRecipe.addIngredient(Material.FLINT)
		gunpowderRecipe.addIngredient(Material.SAND)
		gunpowderRecipe.addIngredient(Material.CHARCOAL)
		server.addRecipe(gunpowderRecipe)

		// Ensure loading is actually complete before continuing.
		if (asyncInit.loadLock.count == 1L) {
			slF4JLogger.info("Async enable incomplete, waiting for completion.")
			asyncInit.loadLock.await()
		}
	}
}