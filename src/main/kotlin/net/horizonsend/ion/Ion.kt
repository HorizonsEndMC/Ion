package net.horizonsend.ion

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.features.ores.OreListener
import net.horizonsend.ion.miscellaneous.ShrugCommand
import net.horizonsend.ion.miscellaneous.listeners.BlockFadeListener
import net.horizonsend.ion.miscellaneous.listeners.BlockFormListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerDeathListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerFishListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerJoinListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerQuitListener
import net.horizonsend.ion.miscellaneous.listeners.PlayerTeleportListener
import net.horizonsend.ion.miscellaneous.listeners.PotionSplashListener
import net.horizonsend.ion.miscellaneous.listeners.PrepareAnvilListener
import net.horizonsend.ion.miscellaneous.listeners.PrepareItemEnchantListener
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Plugin entrypoint
class Ion : JavaPlugin() {
	override fun onEnable() {
		server.scheduler.runTaskAsynchronously(
			this,
			Runnable {
				arrayOf(
					BlockFadeListener(),
					BlockFormListener(),
					PlayerDeathListener(),
					PlayerFishListener(),
					PlayerItemConsumeListener(),
					PlayerJoinListener(),
					PlayerQuitListener(),
					PlayerTeleportListener(),
					PotionSplashListener(),
					PrepareAnvilListener(),
					PrepareItemEnchantListener(),
					OreListener(this)
				).forEach {
					server.pluginManager.registerEvents(it, this)
				}

				PaperCommandManager(this).apply {
					@Suppress("deprecation")
					enableUnstableAPI("help")

					registerCommand(ShrugCommand())
				}
			}
		)

		server.addRecipe(FurnaceRecipe(NamespacedKey(this, "prismarine_bricks_recipe"), ItemStack(Material.PRISMARINE), Material.PRISMARINE_BRICKS, 1f, 200))

		val bellRecipe = ShapedRecipe(NamespacedKey(this, "bell_recipe"), ItemStack(Material.BELL))
		bellRecipe.shape("wow", "szs", "zzz")
		bellRecipe.setIngredient('w', RecipeChoice.MaterialChoice(Material.STICK))
		bellRecipe.setIngredient('o', RecipeChoice.MaterialChoice(Material.OAK_LOG))
		bellRecipe.setIngredient('s', RecipeChoice.MaterialChoice(Material.IRON_BLOCK))
		bellRecipe.setIngredient('z', RecipeChoice.MaterialChoice(Material.GOLD_BLOCK))
		server.addRecipe(bellRecipe)

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

			val gunpowderrecipe = ShapelessRecipe(NamespacedKey(this, "gunpowderrecipe"), ItemStack(Material.GUNPOWDER))
			gunpowderrecipe.addIngredient(Material.REDSTONE)
			gunpowderrecipe.addIngredient(Material.FLINT)
			gunpowderrecipe.addIngredient(Material.SAND)
			gunpowderrecipe.addIngredient(Material.CHARCOAL)
			server.addRecipe(gunpowderrecipe)
		}
	}
}