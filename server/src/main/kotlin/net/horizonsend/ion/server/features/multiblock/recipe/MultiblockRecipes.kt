package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.industry.OxygenFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.GasCanisterIngredient
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.ItemIngredient
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

object MultiblockRecipes : IonServerComponent() {
	private val recipes: ConcurrentHashMap<Multiblock, LinkedList<MultiblockRecipe<*>>> = ConcurrentHashMap()

	val URANIUM_ENRICHMENT = registerRecipe(CentrifugeMultiblock, ProcessingMultiblockRecipe(
		60L * 20L, // 1 minute
		ItemIngredient(CustomItems.URANIUM, 1),
		CentrifugeMultiblock,
		CustomItems.ENRICHED_URANIUM.constructItemStack()
	))

	val URANIUM_CORE_COMPRESSION = registerRecipe(CentrifugeMultiblock, ProcessingMultiblockRecipe(
		60L * 60L * 20L, // 1 hour
		ItemIngredient(CustomItems.URANIUM_CORE, 1),
		CompressorMultiblock,
		CustomItems.URANIUM_ROD.constructItemStack()
	))

	val STEEL_PRODUCTION = registerRecipe(OxygenFurnaceMultiblock, FurnaceMultiblockRecipe(
		100L, // 5 seconds
		ItemIngredient(ItemStack(Material.IRON_INGOT), 1),
		GasCanisterIngredient(CustomItems.GAS_CANISTER_OXYGEN, 100),
		OxygenFurnaceMultiblock,
		CustomItems.STEEL_INGOT.constructItemStack()
	))

	fun <T: Multiblock> registerRecipe(multiblock: Multiblock, recipe: MultiblockRecipe<T>): MultiblockRecipe<T> {
		recipes.getOrPut(multiblock) { LinkedList() }.add(recipe)

		return recipe
	}

	fun <T: Multiblock> check(multiblock: T, sign: Sign, inventory: Inventory): MultiblockRecipe<T>? {
		@Suppress("UNCHECKED_CAST")
		val recipesFor = (recipes[multiblock] as? LinkedList<MultiblockRecipe<T>>) ?: return null

		return recipesFor.firstOrNull { it.matches(multiblock, sign, inventory) }
	}
}
