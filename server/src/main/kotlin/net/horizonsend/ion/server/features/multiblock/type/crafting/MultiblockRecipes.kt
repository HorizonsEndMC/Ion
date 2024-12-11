package net.horizonsend.ion.server.features.multiblock.type.crafting

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient.ConsumedItemIngredient
import net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient.GasCanisterIngredient
import net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient.ProgressHolderItemIngredient
import net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient.ResourceIngredient
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.type.crafting.recipe.ProcessingMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.type.crafting.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.type.crafting.result.MultiRecipeResult
import net.horizonsend.ion.server.features.multiblock.type.crafting.result.ProgressItemResult
import net.horizonsend.ion.server.features.multiblock.type.crafting.result.SoundResult
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.PlatePressMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.getMatchingMaterials
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNUSED")
object MultiblockRecipes : IonServerComponent() {
	private val recipes: ConcurrentHashMap<Multiblock, LinkedList<MultiblockRecipe<*>>> = ConcurrentHashMap()

	val URANIUM_ENRICHMENT = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = CentrifugeMultiblock,
		smelting = ConsumedItemIngredient(CustomItemRegistry.URANIUM, 1),
		result = MultiRecipeResult(
			ItemResult(CustomItems.ENRICHED_URANIUM),
			SoundResult("horizonsend:industry.centrifuge", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
		resources = listOf(power(100)),
	)
	)

	val URANIUM_CORE_COMPRESSION = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = CompressorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.URANIUM_CORE, 1), progressHolderResult = CustomItems.URANIUM_ROD),
		resources = listOf(power(100)),
		result = MultiRecipeResult(
			ProgressItemResult(CustomItems.URANIUM_ROD, 60L * 60L * 20L, SoundResult("horizonsend:industry.compress", SoundCategory.BLOCKS, 1.0f, 1.0f))
		)
	)
	)

	val STEEL_PRODUCTION = registerRecipe(
		FurnaceMultiblockRecipe(
		multiblock = GasFurnaceMultiblock,
		smelting = ConsumedItemIngredient(ItemStack(Material.IRON_INGOT), 1),
		fuel = GasCanisterIngredient(CustomItems.GAS_CANISTER_OXYGEN, 5),
		resources = listOf(power(100)),
		result = ItemResult(CustomItems.STEEL_INGOT)
	)
	)

	val REACTIVE_PLATING_PRESSING = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.REACTIVE_PLATING, 1), progressHolderResult = CustomItems.REACTIVE_CHASSIS),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.REACTIVE_CHASSIS, 60L * 60L * 20L, SoundResult("horizonsend:industry.press", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val STEEL_PLATE_PRESSING = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.STEEL_PLATE, 1), progressHolderResult = CustomItems.STEEL_CHASSIS),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.STEEL_CHASSIS, 60L * 60L * 20L, SoundResult("horizonsend:industry.press", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val FUEL_ROD_CORE_FABRICATION = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.FUEL_ROD_CORE, 1), progressHolderResult = CustomItems.FUEL_CELL),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.FUEL_CELL, 60L * 60L * 20L * 2L, SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val FABRICATED_ASSEMBLY_FABRICATION = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.REACTIVE_ASSEMBLY, 1), progressHolderResult = CustomItems.FABRICATED_ASSEMBLY),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.FABRICATED_ASSEMBLY, 60L * 60L * 20L * 2L, SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val REINFORCED_FRAME_FABRICATION = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.STEEL_ASSEMBLY, 1), progressHolderResult = CustomItems.REINFORCED_FRAME),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.REINFORCED_FRAME, 60L * 60L * 20L * 2L, SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val CIRCUIT_BOARD_FABRICATION = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = CircuitfabMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItemRegistry.CIRCUITRY, 1), progressHolderResult = CustomItems.CIRCUIT_BOARD),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.CIRCUIT_BOARD, 60L * 60L * 20L, SoundResult("horizonsend:industry.cirfab", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val LOADED_SHELL_LOADING = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = AmmoLoaderMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.UNLOADED_SHELL, 1), progressHolderResult = CustomItems.LOADED_SHELL),
		resources = listOf(power(100)),
		result = MultiRecipeResult(ProgressItemResult(CustomItems.LOADED_SHELL, 90L * 20L, SoundResult("horizonsend:industry.load", SoundCategory.BLOCKS, 1.0f, 1.0f)))
	)
	)

	val UNCHARGED_SHELL_CHARGING = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = AmmoLoaderMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.UNCHARGED_SHELL, 1), progressHolderResult = CustomItems.CHARGED_SHELL),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.CHARGED_SHELL, 90L * 20L, SoundResult("horizonsend:industry.load", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	val ARSENAL_MISSILE_LOADING = registerRecipe(
		ProcessingMultiblockRecipe(
		multiblock = MissileLoaderMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.UNLOADED_ARSENAL_MISSILE, 1), progressHolderResult = CustomItems.ARSENAL_MISSILE),
		resources = listOf(power(100)),
		result = ProgressItemResult(CustomItems.ARSENAL_MISSILE, 60L * 60L * 20L, SoundResult("horizonsend:industry.mload", SoundCategory.BLOCKS, 1.0f, 1.0f))
	)
	)

	//BC recipies end

	//misc recipies start

	val COPPER_OXIDATION = registerAllMatch(
		"^(WAXED_|)(CUT|)(_?)(COPPER_BLOCK|COPPER)(.*)",
		setOf(Material.COPPER_ORE,
			Material.DEEPSLATE_COPPER_ORE,
			Material.COPPER_INGOT),
		"^(WAXED_|)(EXPOSED)(.*)",
		"\$1EXPOSED\$3\$2_COPPER\$5"
	)

	val EXPOSED_COPPER_OXIDATION = registerAllMatch(
		"^(WAXED_|)(EXPOSED)(.*)",
		setOf(),
		"^(WAXED_|)(WEATHERED)(.*)",
		"\$1WEATHERED\$3"
	)

	val WEATHERED_COPPER_OXIDATION = registerAllMatch(
		"^(WAXED_|)(WEATHERED)(.*)",
		setOf(),
		"^(WAXED_|)(OXIDIZED)(.*)",
		"\$1OXIDIZED\$3"
	)

	/**
	 * Add a power ingredient
	 **/
	private fun power(amount: Int) = ResourceIngredient(NamespacedKeys.POWER, amount)

	private fun <T: Multiblock> registerRecipe(recipe: MultiblockRecipe<T>): MultiblockRecipe<T> {
		recipes.getOrPut(recipe.multiblock) { LinkedList() }.add(recipe)

		return recipe
	}

	private fun registerAllMatch(
		inputMatch: String, ignore : Set<Material>, outputMatch : String, replaceStr : String
	): Set<MultiblockRecipe<*>> {
		val recipes = mutableSetOf<MultiblockRecipe<*>>()
		val inputMatches = run {
			val list: MutableSet<Material> = getMatchingMaterials { it.name.matches(Regex(inputMatch)) }
			list.removeAll(ignore)
			list.toSet()
		}

		val outputMatches = run {
			val list: MutableSet<Material> = getMatchingMaterials { it.name.matches(Regex(outputMatch)) }
			list.toSet()
		}

		for (inputMat in inputMatches) {
			val resultMat = outputMatches.first { it.name == inputMat.name.replace(Regex(inputMatch),replaceStr) }
			val recipe = registerRecipe(
				FurnaceMultiblockRecipe(
					multiblock = GasFurnaceMultiblock,
					smelting = ConsumedItemIngredient(ItemStack(inputMat), 1),
					fuel = GasCanisterIngredient(CustomItems.GAS_CANISTER_OXYGEN, 50),
					resources = listOf(power(100)),
					result = ItemResult(ItemStack(resultMat))
				)
			)
			recipes.add(recipe)
		}
		return recipes.toSet()
	}

	fun <T: Multiblock> getRecipe(multiblock: T, sign: Sign, inventory: Inventory): MultiblockRecipe<T>? {
		@Suppress("UNCHECKED_CAST")
		val recipesFor = (recipes[multiblock] as? LinkedList<MultiblockRecipe<T>>) ?: return null

		return recipesFor.firstOrNull { it.matches(sign, inventory) }
	}
}
