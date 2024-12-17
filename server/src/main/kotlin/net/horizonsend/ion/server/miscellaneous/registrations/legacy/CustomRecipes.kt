package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.ALUMINUM_INGOT
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CHETHERITE
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.TITANIUM_INGOT
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem as LegacyCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_BLUE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_GREEN
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_ORANGE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_PINK
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_PURPLE
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_RED
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems.ENERGY_SWORD_YELLOW
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.COAL
import org.bukkit.Material.COPPER_INGOT
import org.bukkit.Material.DIAMOND
import org.bukkit.Material.EMERALD
import org.bukkit.Material.ENDER_PEARL
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.END_STONE
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.PINK_TULIP
import org.bukkit.Material.PRISMARINE_CRYSTALS
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.SEA_LANTERN
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
			registerSwordRecipes()
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
		item: LegacyCustomItem,
		vararg shape: String,
		ingredients: Map<Char, RecipeChoice>,
		amount: Int = 1
	): ShapedRecipe = registerShapedRecipe(item.id, item.itemStack(amount), *shape, ingredients = ingredients)

	private fun customItemChoice(customItem: CustomItem): RecipeChoice {
		return RecipeChoice.ExactChoice(customItem.constructItemStack())
	}

	private fun materialChoice(material: Material): RecipeChoice {
		return RecipeChoice.MaterialChoice(material)
	}

	private fun registerSwordRecipes() = mapOf(
		ENERGY_SWORD_BLUE to materialChoice(DIAMOND),
		ENERGY_SWORD_RED to materialChoice(REDSTONE),
		ENERGY_SWORD_YELLOW to materialChoice(COAL),
		ENERGY_SWORD_GREEN to materialChoice(EMERALD),
		ENERGY_SWORD_PURPLE to customItemChoice(CHETHERITE),
		ENERGY_SWORD_ORANGE to materialChoice(COPPER_INGOT),
		ENERGY_SWORD_PINK to materialChoice(PINK_TULIP)
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
