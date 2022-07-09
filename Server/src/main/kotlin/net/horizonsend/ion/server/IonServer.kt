package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.managers.CommonManager
import net.horizonsend.ion.server.commands.GuideCommand
import net.horizonsend.ion.server.listeners.bukkit.BlockFadeListener
import net.horizonsend.ion.server.listeners.bukkit.BlockFormListener
import net.horizonsend.ion.server.listeners.bukkit.ChunkLoadListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryClickListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryCloseListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryDragListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryInteractListener
import net.horizonsend.ion.server.listeners.bukkit.InventoryMoveItemListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerDeathListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerFishListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerJoinListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerKickListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerLoginListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerQuitListener
import net.horizonsend.ion.server.listeners.bukkit.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.bukkit.PotionSplashListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareAnvilListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareItemCraftListener
import net.horizonsend.ion.server.listeners.bukkit.PrepareItemEnchantListener
import net.horizonsend.ion.server.listeners.luckperms.UserDataRecalculateListener
import net.horizonsend.ion.server.utilities.forbiddenCraftingItems
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused") // Plugin entrypoint
class IonServer : JavaPlugin() {
	override fun onEnable() {
		CommonManager.init(dataFolder.toPath())

		/**
		 * Listeners
		 */
		// Bukkit
		arrayOf(
			BlockFadeListener(), BlockFormListener(), ChunkLoadListener(this), InventoryClickListener(),
			InventoryCloseListener(), InventoryDragListener(), InventoryInteractListener(), InventoryMoveItemListener(),
			PlayerDeathListener(), PlayerFishListener(), PlayerItemConsumeListener(), PlayerJoinListener(),
			PlayerKickListener(), PlayerLoginListener(), PlayerQuitListener(), PlayerTeleportListener(),
			PotionSplashListener(), PrepareAnvilListener(), PrepareItemCraftListener(), PrepareItemEnchantListener()
		).forEach { server.pluginManager.registerEvents(it, this) }

		// Luckperms
		UserDataRecalculateListener()

		/**
		 * Recipes
		 */
		// Prismarine Bricks
		server.addRecipe(
			FurnaceRecipe(
				NamespacedKey(this, "prismarine_bricks_recipe"),
				ItemStack(Material.PRISMARINE_BRICKS),
				Material.PRISMARINE,
				1f,
				200
			)
		)

		// Bell
		server.addRecipe(ShapedRecipe(NamespacedKey(this, "bell_recipe"), ItemStack(Material.BELL)).apply {
				shape("wow", "szs", "zzz")
				setIngredient('w', MaterialChoice(Material.STICK))
				setIngredient('o', MaterialChoice(Material.OAK_LOG))
				setIngredient('s', MaterialChoice(Material.IRON_BLOCK))
				setIngredient('z', MaterialChoice(Material.GOLD_BLOCK))
		})

		// Enderpearl
		server.addRecipe(ShapedRecipe(NamespacedKey(this, "enderpearl_recipe"), ItemStack(Material.ENDER_PEARL)).apply {
			shape("wow", "oso", "wow")
			setIngredient('w', MaterialChoice(Material.OBSIDIAN))
			setIngredient('o', MaterialChoice(Material.EMERALD))
			setIngredient('s', MaterialChoice(Material.DIAMOND_BLOCK))
		})

		// Gunpowder
		server.addRecipe(ShapelessRecipe(NamespacedKey(this, "gunpowder_recipe"), ItemStack(Material.GUNPOWDER)).apply {
			addIngredient(Material.REDSTONE)
			addIngredient(Material.FLINT)
			addIngredient(Material.SAND)
			addIngredient(Material.CHARCOAL)
		})

		// Wool -> String
		arrayOf(
			Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL,
			Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL,
			Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL,
			Material.BLACK_WOOL
		).forEach {
			server.addRecipe(
				ShapelessRecipe(
					NamespacedKey(this, "${it.name.lowercase()}_string_recipe"),
					ItemStack(Material.STRING, 4)
				).apply {
					addIngredient(1, it)
				}
			)
		}

		// Remove Unwanted Vanilla Recipes
		forbiddenCraftingItems.forEach { material ->
			server.getRecipesFor(ItemStack(material)).forEach {
				if (it is Keyed) server.removeRecipe(it.key)
			}
		}

		/**
		 * Commands
		 */
		PaperCommandManager(this).apply {
			registerCommand(GuideCommand())
		}
	}
}