package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object CustomRecipes : IonServerComponent() {
	override fun onEnable() {
		Tasks.syncDelay(1) {
			registerMineralRecipes()
			registerBatteryRecipes()
			registerArmorRecipes()
			registerModuleRecipes()
			registerSwordRecipes()
			registerPowerToolRecipes()
			registerGasCanisterRecipe()
			registerDetonatorRecipe()
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

	private fun customItemChoice(customItem: CustomItem): RecipeChoice {
		return RecipeChoice.ExactChoice(customItem.singleItem())
	}

	private fun materialChoice(material: Material): RecipeChoice {
		return RecipeChoice.MaterialChoice(material)
	}

	private fun registerBatteryRecipes() {
		createRecipe(
			CustomItems.BATTERY_SMALL, "aba", "aba", "aba",
			ingredients = mapOf(
				'a' to customItemChoice(CustomItems.MINERAL_ALUMINUM),
				'b' to materialChoice(Material.GLOWSTONE_DUST)
			)
		)
		createRecipe(
			CustomItems.BATTERY_MEDIUM, "aba", "aba", "aba",
			ingredients = mapOf(
				'a' to customItemChoice(CustomItems.MINERAL_ALUMINUM),
				'b' to materialChoice(Material.REDSTONE)
			)
		)
		createRecipe(
			CustomItems.BATTERY_LARGE, "aba", "aba", "aba",
			ingredients = mapOf(
				'a' to customItemChoice(CustomItems.MINERAL_ALUMINUM),
				'b' to materialChoice(Material.SEA_LANTERN)
			)
		)
	}

	private fun registerMineralRecipes() = listOf(
// 		CustomItems.MINERAL_COPPER,
		CustomItems.MINERAL_ALUMINUM,
		CustomItems.MINERAL_TITANIUM,
		CustomItems.MINERAL_URANIUM,
		CustomItems.MINERAL_CHETHERITE
// 		CustomItems.MINERAL_ORIOMIUM
	).forEach { mineral: CustomItems.MineralCustomItem ->
		createShapelessRecipe(
			mineral,
			customItemChoice(mineral.fullBlock),
			amount = 9
		)
		createRecipe(
			mineral.fullBlock, "aaa", "aaa", "aaa", ingredients = mapOf('a' to customItemChoice(mineral))
		)
	}

	private fun registerArmorRecipes() {
		val items = mapOf(
			'*' to customItemChoice(CustomItems.MINERAL_TITANIUM),
			'b' to customItemChoice(CustomItems.BATTERY_LARGE)
		)

		createRecipe(CustomItems.POWER_ARMOR_HELMET, "*b*", "* *", ingredients = items)
		createRecipe(CustomItems.POWER_ARMOR_CHESTPLATE, "* *", "*b*", "***", ingredients = items)
		createRecipe(CustomItems.POWER_ARMOR_LEGGINGS, "*b*", "* *", "* *", ingredients = items)
		createRecipe(CustomItems.POWER_ARMOR_BOOTS, "* *", "*b*", ingredients = items)
	}

	private fun registerModuleRecipes() = mapOf(
		CustomItems.POWER_MODULE_SHOCK_ABSORBING to customItemChoice(CustomItems.MINERAL_TITANIUM),
		CustomItems.POWER_MODULE_SPEED_BOOSTING to materialChoice(Material.FEATHER),
		CustomItems.POWER_MODULE_ROCKET_BOOSTING to materialChoice(Material.FIREWORK_ROCKET),
		CustomItems.POWER_MODULE_NIGHT_VISION to materialChoice(Material.SPIDER_EYE),
		CustomItems.POWER_MODULE_ENVIRONMENT to materialChoice(Material.CHAINMAIL_HELMET),
		CustomItems.POWER_MODULE_PRESSURE_FIELD to customItemChoice(CustomItems.GAS_CANISTER_EMPTY)
	).forEach { (piece, center) ->
		createRecipe(
			piece, "aga", "g*g", "aga",
			ingredients = mapOf(
				'a' to customItemChoice(CustomItems.MINERAL_ALUMINUM),
				'g' to materialChoice(Material.GLASS_PANE),
				'*' to center
			)
		)
	}

	private fun registerSwordRecipes() = mapOf(
		CustomItems.ENERGY_SWORD_BLUE to materialChoice(Material.DIAMOND),
		CustomItems.ENERGY_SWORD_RED to materialChoice(Material.REDSTONE),
		CustomItems.ENERGY_SWORD_YELLOW to materialChoice(Material.COAL),
		CustomItems.ENERGY_SWORD_GREEN to materialChoice(Material.EMERALD),
		CustomItems.ENERGY_SWORD_PURPLE to customItemChoice(CustomItems.MINERAL_CHETHERITE),
		CustomItems.ENERGY_SWORD_ORANGE to materialChoice(Material.COPPER_INGOT)
	).forEach { (sword, specialItem) ->
		createRecipe(
			sword, "aga", "a*a", "ata",
			ingredients = mapOf(
				'a' to customItemChoice(CustomItems.MINERAL_ALUMINUM),
				'g' to materialChoice(Material.GLASS_PANE),
				'*' to specialItem,
				't' to customItemChoice(CustomItems.MINERAL_TITANIUM)
			)
		)
	}

	private fun registerPowerToolRecipes() {
		createRecipe(
			CustomItems.POWER_TOOL_DRILL, "i  ", " bt", " ts",
			ingredients = mapOf(
				'i' to materialChoice(Material.IRON_INGOT),
				'b' to customItemChoice(CustomItems.BATTERY_MEDIUM),
				't' to customItemChoice(CustomItems.MINERAL_TITANIUM),
				's' to materialChoice(Material.STICK)
			)
		)

		createRecipe(
			CustomItems.POWER_TOOL_CHAINSAW, "ai ", "ibt", " ts",
			ingredients = mapOf(
				'a' to customItemChoice(CustomItems.MINERAL_ALUMINUM),
				'i' to materialChoice(Material.IRON_INGOT),
				'b' to customItemChoice(CustomItems.BATTERY_MEDIUM),
				't' to customItemChoice(CustomItems.MINERAL_TITANIUM),
				's' to materialChoice(Material.STICK)
			)
		)
	}

	private fun registerGasCanisterRecipe() {
		createRecipe(
			CustomItems.GAS_CANISTER_EMPTY, " i ", "igi", " i ",
			ingredients = mapOf(
				'i' to customItemChoice(CustomItems.MINERAL_TITANIUM),
				'g' to materialChoice(Material.GLASS_PANE)
			)
		)
	}

	private fun registerDetonatorRecipe() {
		createRecipe(
			CustomItems.DETONATOR, " r ", "tut", " t ",
			ingredients = mapOf(
				'r' to materialChoice(Material.REDSTONE),
				't' to customItemChoice(CustomItems.MINERAL_TITANIUM),
				'u' to customItemChoice(CustomItems.MINERAL_URANIUM)
			)
		)
	}

	private fun registerWireRecipe() {
		registerShapedRecipe(
			"end_rod",
			ItemStack(Material.END_ROD, 16), "ccc",
			ingredients = mapOf(
				'c' to materialChoice(Material.COPPER_INGOT)
			)
		)
	}

	private fun registerSeaLanternRecipe() {
		registerShapelessRecipe(
			"sea_lantern",
			ItemStack(Material.SEA_LANTERN, 1),
			materialChoice(Material.PRISMARINE_CRYSTALS),
			materialChoice(Material.PRISMARINE_CRYSTALS),
			materialChoice(Material.PRISMARINE_CRYSTALS),
			materialChoice(Material.PRISMARINE_CRYSTALS)
		)
	}

	private fun registerEndPortalFrameRecipe() {
		registerShapedRecipe(
			"end_portal_frame",
			ItemStack(Material.END_PORTAL_FRAME, 1),
			"wow", "sss",
			ingredients = mapOf(
				'w' to materialChoice(Material.WARPED_PLANKS),
				'o' to materialChoice(Material.ENDER_PEARL),
				's' to materialChoice(Material.END_STONE)
			)
		)
	}
}
