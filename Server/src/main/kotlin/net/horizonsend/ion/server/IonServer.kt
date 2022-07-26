package net.horizonsend.ion.server

import co.aikar.commands.PaperCommandManager
import net.horizonsend.ion.common.managers.CommonManager
import net.horizonsend.ion.server.commands.GuideCommand
import net.horizonsend.ion.server.listeners.luckperms.UserDataRecalculateListener
import net.horizonsend.ion.server.utilities.forbiddenCraftingItems
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.Scanners.SubTypes

@Suppress("Unused")
class IonServer : JavaPlugin() {
	override fun onEnable() {
		CommonManager.init(dataFolder.toPath())

		val reflectionsScanner = Reflections("net.horizonsend.ion.server")

		reflectionsScanner.get(SubTypes.of(Listener::class.java).asClass<Listener>())
			// TODO: Listeners should not be handling state so directly, they should not need the plugin instance.
			.map {
				val parameters = it.constructors[0].parameterTypes.map { type -> when (type) {
					IonServer::class.java -> this
					else -> throw NotImplementedError("Can not provide ${type.simpleName}")
				}}.toTypedArray()

				it.constructors[0].newInstance(*parameters)
			}
			.forEach {
				server.pluginManager.registerEvents(it as Listener, this)
			}

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

		// Saddle
		server.addRecipe(ShapedRecipe(NamespacedKey(this, "Saddle_Recipe"), ItemStack(Material.SADDLE)).apply {
			shape("lll", "tat")
			setIngredient('l', Material.LEATHER)
			setIngredient('t', Material.TRIPWIRE)
			setIngredient('a', Material.AIR)
		})

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