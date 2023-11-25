package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems.ALUMINUM_INGOT
import net.horizonsend.ion.server.features.customitems.CustomItems.CHETHERITE
import net.horizonsend.ion.server.features.customitems.CustomItems.TITANIUM_INGOT
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.BATTERY_LARGE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.BATTERY_MEDIUM
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_BLUE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_GREEN
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_ORANGE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_PURPLE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_RED
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_YELLOW
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_ARMOR_BOOTS
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_ARMOR_CHESTPLATE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_ARMOR_HELMET
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_ARMOR_LEGGINGS
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_MODULE_ENVIRONMENT
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_MODULE_NIGHT_VISION
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_MODULE_PRESSURE_FIELD
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_MODULE_ROCKET_BOOSTING
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_MODULE_SHOCK_ABSORBING
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_MODULE_SPEED_BOOSTING
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_TOOL_CHAINSAW
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.POWER_TOOL_DRILL
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.CHAINMAIL_HELMET
import org.bukkit.Material.COAL
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.DIAMOND
import org.bukkit.Material.EMERALD
import org.bukkit.Material.ENDER_PEARL
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.END_STONE
import org.bukkit.Material.FEATHER
import org.bukkit.Material.FIREWORK_ROCKET
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GLOWSTONE_DUST
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SPIDER_EYE
import org.bukkit.Material.STICK
import org.bukkit.Material.WARPED_PLANKS
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object CustomRecipes : IonServerComponent() {
	override fun onEnable() {
		Tasks.syncDelay(1) {
			registerBatteryRecipes()
			registerArmorRecipes()
			registerModuleRecipes()
			registerSwordRecipes()
			registerPowerToolRecipes()
			registerWireRecipe()
			registerSeaLanternRecipe()
			registerEndPortalFrameRecipe()
		}
	}

	private fun registerShapedRecipe(
		id: String,
		output: ItemStack,
		vararg shape: String,
		ingredients: Map<Char, RecipeChoice>
	): ShapedRecipe {
		val key = NamespacedKey(IonServer, id)

		val recipe = ShapedRecipe(key, output)

		recipe.shape(*shape)

		for ((char, ingredient: RecipeChoice) in ingredients) {
			recipe.setIngredient(char, ingredient)
		}

		addRecipe(recipe, key)

		return recipe
	}

	private fun registerShapelessRecipe(
		id: String,
		output: ItemStack,
		vararg ingredients: RecipeChoice
	): ShapelessRecipe {
		check(ingredients.isNotEmpty())

		val key = NamespacedKey(IonServer, id)

		val recipe = ShapelessRecipe(key, output)

		for (ingredient in ingredients) {
			recipe.addIngredient(ingredient)
		}

		addRecipe(recipe, key)

		return recipe
	}

	private fun addRecipe(recipe: Recipe, key: NamespacedKey, attempt: Int = 1) {
		Bukkit.getServer().addRecipe(recipe)
		if (attempt > 1) {
			val added = Bukkit.getServer().getRecipe(key) != null
			log.info("Recipe $key Added (attempt $attempt): ${(added)}")
		}
		Tasks.syncDelay(1) {
			val kept = Bukkit.getServer().getRecipe(key) != null
			if (attempt > 1) {
				log.info("Recipe $kept Kept (attempt $attempt): ${(kept)}")
			}
			if (!kept) {
				addRecipe(recipe, key, attempt + 1)
			}
		}
	}

	private fun createRecipe(
		item: CustomItem,
		vararg shape: String,
		ingredients: Map<Char, RecipeChoice>,
		amount: Int = 1
	): ShapedRecipe = registerShapedRecipe(item.id, item.itemStack(amount), *shape, ingredients = ingredients)

	private fun createShapelessRecipe(
		item: CustomItem,
		vararg ingredients: RecipeChoice,
		amount: Int = 1
	): ShapelessRecipe = registerShapelessRecipe(item.id, item.itemStack(amount), *ingredients)

	private fun legacyCustomItemChoice(customItem: CustomItem): RecipeChoice {
		return RecipeChoice.ExactChoice(customItem.singleItem())
	}

	private fun customItemChoice(customItem: net.horizonsend.ion.server.features.customitems.CustomItem): RecipeChoice {
		return RecipeChoice.ExactChoice(customItem.constructItemStack())
	}

	private fun materialChoice(material: Material): RecipeChoice {
		return RecipeChoice.MaterialChoice(material)
	}

	private fun registerBatteryRecipes() {
		createRecipe(
			CustomItems.BATTERY_SMALL, "aba", "aba", "aba",
			ingredients = mapOf(
				'a' to customItemChoice(ALUMINUM_INGOT),
				'b' to materialChoice(GLOWSTONE_DUST)
			)
		)
		createRecipe(
			BATTERY_MEDIUM, "aba", "aba", "aba",
			ingredients = mapOf(
				'a' to customItemChoice(ALUMINUM_INGOT),
				'b' to materialChoice(REDSTONE)
			)
		)
		createRecipe(
			BATTERY_LARGE, "aba", "aba", "aba",
			ingredients = mapOf(
				'a' to customItemChoice(ALUMINUM_INGOT),
				'b' to materialChoice(SEA_LANTERN)
			)
		)
	}

	private fun registerArmorRecipes() {
		val items = mapOf(
			'*' to customItemChoice(TITANIUM_INGOT),
			'b' to legacyCustomItemChoice(BATTERY_LARGE)
		)

		createRecipe(POWER_ARMOR_HELMET, "*b*", "* *", ingredients = items)
		createRecipe(POWER_ARMOR_CHESTPLATE, "* *", "*b*", "***", ingredients = items)
		createRecipe(POWER_ARMOR_LEGGINGS, "*b*", "* *", "* *", ingredients = items)
		createRecipe(POWER_ARMOR_BOOTS, "* *", "*b*", ingredients = items)
	}

	private fun registerModuleRecipes() = mapOf(
		POWER_MODULE_SHOCK_ABSORBING to customItemChoice(TITANIUM_INGOT),
		POWER_MODULE_SPEED_BOOSTING to materialChoice(FEATHER),
		POWER_MODULE_ROCKET_BOOSTING to materialChoice(FIREWORK_ROCKET),
		POWER_MODULE_NIGHT_VISION to materialChoice(SPIDER_EYE),
		POWER_MODULE_ENVIRONMENT to materialChoice(CHAINMAIL_HELMET),
		POWER_MODULE_PRESSURE_FIELD to RecipeChoice.ExactChoice(net.horizonsend.ion.server.features.customitems.CustomItems.GAS_CANISTER_EMPTY.constructItemStack())
	).forEach { (piece, center) ->
		createRecipe(
			piece, "aga", "g*g", "aga",
			ingredients = mapOf(
				'a' to customItemChoice(ALUMINUM_INGOT),
				'g' to materialChoice(GLASS_PANE),
				'*' to center
			)
		)
	}

	private fun registerSwordRecipes() = mapOf(
		ENERGY_SWORD_BLUE to materialChoice(DIAMOND),
		ENERGY_SWORD_RED to materialChoice(REDSTONE),
		ENERGY_SWORD_YELLOW to materialChoice(COAL),
		ENERGY_SWORD_GREEN to materialChoice(EMERALD),
		ENERGY_SWORD_PURPLE to customItemChoice(CHETHERITE),
		ENERGY_SWORD_ORANGE to materialChoice(COPPER_INGOT)
	).forEach { (sword, specialItem) ->
		createRecipe(
			sword, "aga", "a*a", "ata",
			ingredients = mapOf(
				'a' to customItemChoice(ALUMINUM_INGOT),
				'g' to materialChoice(GLASS_PANE),
				'*' to specialItem,
				't' to customItemChoice(TITANIUM_INGOT)
			)
		)
	}

	private fun registerPowerToolRecipes() {
		createRecipe(
			POWER_TOOL_DRILL, "i  ", " bt", " ts",
			ingredients = mapOf(
				'i' to materialChoice(IRON_INGOT),
				'b' to legacyCustomItemChoice(BATTERY_MEDIUM),
				't' to customItemChoice(TITANIUM_INGOT),
				's' to materialChoice(STICK)
			)
		)

		createRecipe(
			POWER_TOOL_CHAINSAW, "ai ", "ibt", " ts",
			ingredients = mapOf(
				'a' to customItemChoice(ALUMINUM_INGOT),
				'i' to materialChoice(IRON_INGOT),
				'b' to legacyCustomItemChoice(BATTERY_MEDIUM),
				't' to customItemChoice(TITANIUM_INGOT),
				's' to materialChoice(STICK)
			)
		)
	}

	private fun registerWireRecipe() {
		registerShapedRecipe(
			"end_rod",
			ItemStack(END_ROD, 16), "ccc",
			ingredients = mapOf(
				'c' to materialChoice(COPPER_INGOT)
			)
		)
	}

	private fun registerSeaLanternRecipe() {
		registerShapelessRecipe(
			"sea_lantern",
			ItemStack(SEA_LANTERN, 1),
			materialChoice(PRISMARINE_CRYSTALS),
			materialChoice(PRISMARINE_CRYSTALS),
			materialChoice(PRISMARINE_CRYSTALS),
			materialChoice(PRISMARINE_CRYSTALS)
		)
	}

	private fun registerEndPortalFrameRecipe() {
		registerShapedRecipe(
			"end_portal_frame",
			ItemStack(END_PORTAL_FRAME, 1),
			"wow", "sss",
			ingredients = mapOf(
				'w' to materialChoice(WARPED_PLANKS),
				'o' to materialChoice(ENDER_PEARL),
				's' to materialChoice(END_STONE)
			)
		)
	}
}
