package net.horizonsend.ion.server.features.multiblock.crafting

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ConsumedItemIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.GasCanisterIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ProgressHolderItemIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ResourceIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.ProcessingMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.ProgressItemResult
import net.horizonsend.ion.server.features.multiblock.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.PlatePressMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.Material
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
		result = ItemResult(CustomItems.ENRICHED_URANIUM),
		resources = listOf(power(100)),
	))

	val URANIUM_CORE_COMPRESSION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CompressorMultiblock,
		smelting = ConsumedItemIngredient(CustomItems.URANIUM_CORE, 1),
		result = ItemResult(CustomItems.URANIUM_ROD),
		resources = listOf(power(100_000)),
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
		smelting = ConsumedItemIngredient(CustomItems.REACTIVE_PLATING, 1),
		resources = listOf(power(100_000 / 60 * 60 * 20)),
		result = ProgressItemResult(CustomItems.REACTIVE_CHASSIS, 60L * 60L * 20L)
	))

	val STEEL_PLATE_PRESSING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		smelting = ProgressHolderItemIngredient(ConsumedItemIngredient(CustomItems.STEEL_PLATE, 1), CustomItems.STEEL_CHASSIS),
		resources = listOf(power(100_000 / 60 * 60 * 20)),
		result = ProgressItemResult(CustomItems.STEEL_CHASSIS, 60L * 60L * 20L)
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
