package net.horizonsend.ion.server.features.multiblock.crafting

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ConsumedItemIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.GasCanisterIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ProgressHolderItemIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ResourceIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.ProcessingMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.MultiRecipeResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.ProgressItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.SoundResult
import net.horizonsend.ion.server.features.multiblock.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.PlatePressMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNUSED")
object MultiblockRecipes : IonServerComponent() {
	private val recipes: ConcurrentHashMap<Multiblock, LinkedList<MultiblockRecipe<*>>> = ConcurrentHashMap()

	val URANIUM_ENRICHMENT = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CentrifugeMultiblock,
		smelting = ConsumedItemIngredient(CustomItems.URANIUM, 1),
		result = MultiRecipeResult(
			ItemResult(CustomItems.ENRICHED_URANIUM),
			SoundResult(Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
		resources = listOf(power(100)),
	))

	val URANIUM_CORE_COMPRESSION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CompressorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.URANIUM_CORE, 1), progressHolderResult = CustomItems.URANIUM_ROD),
		resources = listOf(power(150)),
		result = MultiRecipeResult(
			ProgressItemResult(CustomItems.URANIUM_ROD, 60L * 60L * 20L, SoundResult(Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f))
		)
	))

	val STEEL_PRODUCTION = registerRecipe(FurnaceMultiblockRecipe(
		multiblock = GasFurnaceMultiblock,
		smelting = ConsumedItemIngredient(ItemStack(Material.IRON_INGOT), 1),
		fuel = GasCanisterIngredient(CustomItems.GAS_CANISTER_OXYGEN, 100),
		resources = listOf(power(150)),
		result = ItemResult(CustomItems.STEEL_INGOT)
	))

	val REACTIVE_PLATING_PRESSING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.REACTIVE_PLATING, 1), progressHolderResult = CustomItems.REACTIVE_CHASSIS),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.REACTIVE_CHASSIS, 60L * 60L * 20L)
	))

	val STEEL_PLATE_PRESSING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.STEEL_PLATE, 1), progressHolderResult = CustomItems.STEEL_CHASSIS),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.STEEL_CHASSIS, 60L * 60L * 20L)
	))

	val FUEL_ROD_CORE_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.FUEL_ROD_CORE, 1), progressHolderResult = CustomItems.FUEL_CELL),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.FUEL_CELL, 60L * 60L * 20L)
	))

	val FABRICATED_ASSEMBLY_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.REACTIVE_ASSEMBLY, 1), progressHolderResult = CustomItems.FABRICATED_ASSEMBLY),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.FABRICATED_ASSEMBLY, 60L * 60L * 20L * 2L)
	))

	val REINFORCED_FRAME_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.STEEL_ASSEMBLY, 1), progressHolderResult = CustomItems.REINFORCED_FRAME),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.STEEL_ASSEMBLY, 60L * 60L * 20L)
	))

	val CIRCUIT_BOARD_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CircuitfabMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.CIRCUITRY, 1), progressHolderResult = CustomItems.ENHANCED_CIRCUITRY),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.ENHANCED_CIRCUITRY, 60L * 60L * 20L)
	))

	val LOADED_SHELL_LOADING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = AmmoLoaderMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.UNLOADED_TURRET_SHELL, 1), progressHolderResult = CustomItems.LOADED_TURRET_SHELL),
		resources = listOf(power(150)),
		result = MultiRecipeResult(ProgressItemResult(CustomItems.LOADED_TURRET_SHELL, 90L * 20L, SoundResult(Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.BLOCKS, 1.0f, 1.0f)))
	))

	val UNCHARGED_SHELL_CHARGING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = AmmoLoaderMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.UNCHARGED_SHELL, 1), progressHolderResult = CustomItems.CHARGED_SHELL),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.CHARGED_SHELL, 90L * 20L)
	))

	val ARSENAL_MISSILE_LOADING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = MissileLoaderMultiblock,
		smelting = ProgressHolderItemIngredient(initialIngredient = ConsumedItemIngredient(CustomItems.UNLOADED_MISSILE, 1), progressHolderResult = CustomItems.ARSENAL_MISSILE),
		resources = listOf(power(150)),
		result = ProgressItemResult(CustomItems.ARSENAL_MISSILE, 60L * 60L * 20L)
	))

	/**
	 * Add a power ingredient
	 **/
	private fun power(amount: Int) = ResourceIngredient(NamespacedKeys.POWER, amount)

	private fun <T: Multiblock> registerRecipe(recipe: MultiblockRecipe<T>): MultiblockRecipe<T> {
		recipes.getOrPut(recipe.multiblock) { LinkedList() }.add(recipe)

		return recipe
	}

	fun <T: Multiblock> getRecipe(multiblock: T, sign: Sign, inventory: Inventory): MultiblockRecipe<T>? {
		@Suppress("UNCHECKED_CAST")
		val recipesFor = (recipes[multiblock] as? LinkedList<MultiblockRecipe<T>>) ?: return null

		return recipesFor.firstOrNull { it.matches(sign, inventory) }
	}
}
