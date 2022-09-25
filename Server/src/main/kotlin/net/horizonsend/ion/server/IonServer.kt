package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.initializeDatabase
import net.horizonsend.ion.core.bridge
import net.horizonsend.ion.server.utilities.forbiddenCraftingItems
import net.horizonsend.ion.server.utilities.ionCore
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin

// Special Exception Wildcard Imports
import net.horizonsend.ion.server.listeners.bukkit.*
import net.horizonsend.ion.server.listeners.ioncore.*

@Suppress("Unused")
class IonServer : JavaPlugin() {
	override fun onEnable() {
		initializeDatabase(dataFolder)

		bridge = BridgeImplementation()

		val pluginManager = server.pluginManager

		// Bukkit Listener Registration
		val listeners = arrayOf(
			BlockFadeListener(), BlockFormListener(), ChunkLoadListener(this), EnchantItemListener(),
			InventoryClickListener(), InventoryCloseListener(), InventoryDragListener(), InventoryMoveItemListener(),
			PlayerDeathListener(), PlayerFishListener(), PlayerItemConsumeListener(), PlayerJoinListener(this),
			PlayerLoginListener(), PlayerPickUpItemListener(), PlayerQuitListener(), PlayerResourcePackStatusListener(),
			PlayerTeleportListener(), PotionSplashListener(), PrepareItemCraftListener(), PrepareItemEnchantListener()
		)

		for (listener in listeners) {
			pluginManager.registerEvents(listener, this)
		}

		// IonCore Listener Registration
		ionCore {
			val ionCoreListeners = arrayOf(
				BuySpawnShuttleListener(), CaptureStationListener(), CompleteCargoRunListener(), CreateNationListener(),
				CreateNationOutpostListener(), CreateSettlementListener(), DetectShipListener(), EnterPlanetListener(),
				HyperspaceEnterListener(), LevelUpListener(), MultiblockDetectListener(), ShipKillListener(),
				StationSiegeBeginListener()
			)

			for (listener in ionCoreListeners) {
				pluginManager.registerEvents(listener, this)
			}
		}

		val commandManager = PaperCommandManager(this)

		commandManager.registerCommand(AchievementsCommand())

		commandManager.commandCompletions.registerStaticCompletion("achievements", Achievement.values().map { it.name })

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
			Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
			Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
			Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL,
			Material.RED_WOOL, Material.BLACK_WOOL
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

		// Saddle
		server.addRecipe(ShapedRecipe(NamespacedKey(this, "Saddle_Recipe"), ItemStack(Material.SADDLE)).apply {
			shape("lll", "tat")
			setIngredient('l', Material.LEATHER)
			setIngredient('t', Material.TRIPWIRE)
			setIngredient('a', Material.AIR)
		})

		//black dye
		server.addRecipe(ShapelessRecipe(NamespacedKey(this, "Coal_Black_Dye_Recipe"), ItemStack(Material.BLACK_DYE)).apply {
			addIngredient(1, Material.COAL)
		})

		// Remove Unwanted Vanilla Recipes
		forbiddenCraftingItems.forEach { material ->
			server.getRecipesFor(ItemStack(material)).forEach {
				if (it is Keyed) server.removeRecipe(it.key)
			}
		}
	}
}