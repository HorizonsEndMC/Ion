package net.horizonsend.ion.server.features.multiblock.crafting

import io.papermc.paper.util.Tick
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ItemIngredient.Companion.furnaceFuelIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.ItemIngredient.Companion.furnaceSmeltingIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.ingredient.PowerIngredient
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.ProcessingMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.result.ItemResult.Companion.furnaceRecipeResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.MultiRecipeResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.ProgressResult
import net.horizonsend.ion.server.features.multiblock.crafting.result.SoundResult
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.PlatePressMultiblock
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.inventory.ItemStack
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Suppress("UNUSED")
object MultiblockRecipes : IonServerComponent() {
	private val recipes: ConcurrentHashMap<KClass<out MultiblockEntity>, LinkedList<MultiblockRecipe<*>>> = ConcurrentHashMap()

	val URANIUM_ENRICHMENT = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CentrifugeMultiblock,
		multiblockEntityClass = CentrifugeMultiblock.CentrifugeMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.URANIUM, 1),
		result = MultiRecipeResult(
			furnaceRecipeResult(CustomItems.ENRICHED_URANIUM),
			SoundResult("horizonsend:industry.centrifuge", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
		resources = listOf(PowerIngredient(100)),
	))

	val URANIUM_CORE_COMPRESSION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CompressorMultiblock,
		multiblockEntityClass = CompressorMultiblock.CompressorMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.URANIUM_CORE, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L), finalResult = furnaceRecipeResult(CustomItems.URANIUM_ROD)),
			SoundResult("horizonsend:industry.compress", SoundCategory.BLOCKS, 1.0f, 1.0f)
		)
	))

	val STEEL_PRODUCTION = registerRecipe(FurnaceMultiblockRecipe(
		multiblock = GasFurnaceMultiblock,
		multiblockEntityClass = GasFurnaceMultiblock.GasFurnaceMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(ItemStack(Material.IRON_INGOT), 1),
		fuel = furnaceFuelIngredient(CustomItems.GAS_CANISTER_OXYGEN, 5),
		resources = listOf(PowerIngredient(100)),
		result = furnaceRecipeResult(CustomItems.STEEL_INGOT)
	))

	val REACTIVE_PLATING_PRESSING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		multiblockEntityClass = PlatePressMultiblock.PlatePressMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.REACTIVE_PLATING, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L), finalResult = furnaceRecipeResult(CustomItems.REACTIVE_CHASSIS)),
			SoundResult("horizonsend:industry.press", SoundCategory.BLOCKS, 1.0f, 1.0f)
		)
	))

	val STEEL_PLATE_PRESSING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = PlatePressMultiblock,
		multiblockEntityClass = PlatePressMultiblock.PlatePressMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.STEEL_PLATE, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L), finalResult = furnaceRecipeResult(CustomItems.STEEL_CHASSIS)),
			SoundResult("horizonsend:industry.press", SoundCategory.BLOCKS, 1.0f, 1.0f)
		)
	))

	val FUEL_ROD_CORE_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		multiblockEntityClass = FabricatorMultiblock.FabricatorMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.FUEL_ROD_CORE, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L * 2L), finalResult = furnaceRecipeResult(CustomItems.FUEL_CELL)),
			SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	val FABRICATED_ASSEMBLY_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		multiblockEntityClass = FabricatorMultiblock.FabricatorMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.REACTIVE_ASSEMBLY, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L * 2L), finalResult = furnaceRecipeResult(CustomItems.FABRICATED_ASSEMBLY)),
			SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	val REINFORCED_FRAME_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = FabricatorMultiblock,
		multiblockEntityClass = FabricatorMultiblock.FabricatorMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.STEEL_ASSEMBLY, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L * 2L), finalResult = furnaceRecipeResult(CustomItems.REINFORCED_FRAME)),
			SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	val CIRCUIT_BOARD_FABRICATION = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = CircuitfabMultiblock,
		multiblockEntityClass = CircuitfabMultiblock.CircuitfabMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.CIRCUITRY, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L), finalResult = furnaceRecipeResult(CustomItems.CIRCUIT_BOARD)),
			SoundResult("horizonsend:industry.fabricate", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	val LOADED_SHELL_LOADING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = AmmoLoaderMultiblock,
		multiblockEntityClass = AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.UNLOADED_SHELL, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(90L * 20L), finalResult = furnaceRecipeResult(CustomItems.LOADED_SHELL)),
			SoundResult("horizonsend:industry.load", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	val UNCHARGED_SHELL_CHARGING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = AmmoLoaderMultiblock,
		multiblockEntityClass = AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.UNCHARGED_SHELL, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(90L * 20L), finalResult = furnaceRecipeResult(CustomItems.CHARGED_SHELL)),
			SoundResult("horizonsend:industry.load", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	val ARSENAL_MISSILE_LOADING = registerRecipe(ProcessingMultiblockRecipe(
		multiblock = MissileLoaderMultiblock,
		multiblockEntityClass = MissileLoaderMultiblock.MissileLoaderMultiblockEntity::class,
		smelting = furnaceSmeltingIngredient(CustomItems.UNLOADED_ARSENAL_MISSILE, 1),
		resources = listOf(PowerIngredient(100)),
		result = MultiRecipeResult(
			ProgressResult(Tick.of(60L * 60L * 20L), finalResult = furnaceRecipeResult(CustomItems.ARSENAL_MISSILE)),
			SoundResult("horizonsend:industry.mload", SoundCategory.BLOCKS, 1.0f, 1.0f)
		),
	))

	private fun <T: MultiblockEntity> registerRecipe(recipe: MultiblockRecipe<T>): MultiblockRecipe<T> {
		recipes.getOrPut(recipe.entityClass) { LinkedList() }.add(recipe)

		return recipe
	}

	fun <T: MultiblockEntity> getRecipe(entity: T): MultiblockRecipe<T>? {
		@Suppress("UNCHECKED_CAST")
		val recipesFor = (recipes[entity::class] as? LinkedList<MultiblockRecipe<T>>) ?: return null

		return recipesFor.shuffled().firstOrNull { it.canExecute(entity) }
	}
}
