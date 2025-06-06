package net.horizonsend.ion.server.features.multiblock.crafting

import io.papermc.paper.util.Tick
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.AutoMasonRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.GasCanisterRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.WarmupResult
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.ammo.AmmoLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CircuitfabMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.FabricatorMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.GasFurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.PlatePressMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.SoundCategory
import java.time.Duration
import kotlin.reflect.KClass

@Suppress("UNUSED")
object MultiblockRecipeRegistry : IonServerComponent() {
	private val recipes = mutableListOf<MultiblockRecipe<*>>()
	private val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, MultiblockRecipe<*>>()

	override fun onEnable() {
		registerGasFurnaceRecipes()
		registerAutoMasonRecipes()
	}

	val URANIUM_ENRICHMENT = register(FurnaceMultiblockRecipe(
		identifier = "URANIUM_ENRICHMENT",
		clazz = CentrifugeMultiblock.CentrifugeMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.URANIUM),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			duration = Duration.ofSeconds(10),
			normalResult = ItemResult.simpleResult(CustomItemRegistry.ENRICHED_URANIUM),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.centrifuge"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateFurnace()
	))

	val URANIUM_CORE_COMPRESSION = register(FurnaceMultiblockRecipe(
		identifier = "URANIUM_CORE_COMPRESSION",
		clazz = CompressorMultiblock.CompressorMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.URANIUM_CORE),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L),
			ItemResult.simpleResult(CustomItemRegistry.URANIUM_ROD),
		))
			.updateProgressText()
			.updateFurnace()
	))

	val STEEL_PRODUCTION = register(FurnaceMultiblockRecipe(
		identifier = "STEEL_PRODUCTION",
		clazz = GasFurnaceMultiblock.GasFurnaceMultiblockEntity::class,
		smeltingItem = ItemRequirement.MaterialRequirement(Material.IRON_INGOT),
		fuelItem = GasCanisterRequirement(Gasses.OXYGEN, 5),
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			duration = Duration.ofSeconds(10),
			normalResult = ItemResult.simpleResult(CustomItemRegistry.STEEL_INGOT),
		))
			.updateFurnace()
	))

	val REACTIVE_PLATING_PRESSING = register(FurnaceMultiblockRecipe(
		identifier = "REACTIVE_PLATING_PRESSING",
		clazz = PlatePressMultiblock.PlatePressMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.REACTIVE_PLATING),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L),
			ItemResult.simpleResult(CustomItemRegistry.REACTIVE_CHASSIS),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.press"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val STEEL_PLATE_PRESSING = register(FurnaceMultiblockRecipe(
		identifier = "STEEL_PLATE_PRESSING",
		clazz = PlatePressMultiblock.PlatePressMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.STEEL_PLATE),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L),
			ItemResult.simpleResult(CustomItemRegistry.STEEL_CHASSIS),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.press"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val FUEL_ROD_CORE_FABRICATION = register(FurnaceMultiblockRecipe(
		identifier = "FUEL_ROD_CORE_FABRICATION",
		clazz = FabricatorMultiblock.FabricatorMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.FUEL_ROD_CORE),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L * 2L),
			ItemResult.simpleResult(CustomItemRegistry.FUEL_CELL),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val FABRICATED_ASSEMBLY_FABRICATION = register(FurnaceMultiblockRecipe(
		identifier = "FABRICATED_ASSEMBLY_FABRICATION",
		clazz = FabricatorMultiblock.FabricatorMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.REACTIVE_ASSEMBLY),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L * 2L),
			ItemResult.simpleResult(CustomItemRegistry.FABRICATED_ASSEMBLY),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val REINFORCED_FRAME_FABRICATION = register(FurnaceMultiblockRecipe(
		identifier = "REINFORCED_FRAME_FABRICATION",
		clazz = FabricatorMultiblock.FabricatorMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.STEEL_ASSEMBLY),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L * 2L),
			ItemResult.simpleResult(CustomItemRegistry.REINFORCED_FRAME),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val CIRCUIT_BOARD_FABRICATION = register(FurnaceMultiblockRecipe(
		identifier = "CIRCUIT_BOARD_FABRICATION",
		clazz = CircuitfabMultiblock.CircuitfabMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.CIRCUITRY),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L),
			ItemResult.simpleResult(CustomItemRegistry.CIRCUIT_BOARD),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val LOADED_SHELL_LOADING = register(FurnaceMultiblockRecipe(
		identifier = "LOADED_SHELL_LOADING",
		clazz = AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.UNLOADED_SHELL),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Duration.ofSeconds(90),
			ItemResult.simpleResult(CustomItemRegistry.LOADED_SHELL),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.load"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val UNCHARGED_SHELL_CHARGING = register(FurnaceMultiblockRecipe(
		identifier = "UNCHARGED_SHELL_CHARGING",
		clazz = AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.UNCHARGED_SHELL),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Duration.ofSeconds(90),
			ItemResult.simpleResult(CustomItemRegistry.CHARGED_SHELL),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.load"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	val ARSENAL_MISSILE_LOADING = register(FurnaceMultiblockRecipe(
		identifier = "ARSENAL_MISSILE_LOADING",
		clazz = MissileLoaderMultiblock.MissileLoaderMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.UNLOADED_ARSENAL_MISSILE),
		fuelItem = null,
		power = PowerRequirement(10),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Duration.ofMinutes(60),
			ItemResult.simpleResult(CustomItemRegistry.ARSENAL_MISSILE),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.mload"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateProgressText()
			.updateFurnace()
	))

	private fun registerGasFurnaceRecipes() {
		val pairs = arrayOf(
			Material.COPPER_BLOCK to Material.EXPOSED_COPPER,
			Material.EXPOSED_COPPER to Material.WEATHERED_COPPER,
			Material.WEATHERED_COPPER to Material.OXIDIZED_COPPER,

			Material.CHISELED_COPPER to Material.EXPOSED_CHISELED_COPPER,
			Material.EXPOSED_CHISELED_COPPER to Material.WEATHERED_CHISELED_COPPER,
			Material.WEATHERED_CHISELED_COPPER to Material.OXIDIZED_CHISELED_COPPER,

			Material.COPPER_GRATE to Material.EXPOSED_COPPER_GRATE,
			Material.EXPOSED_COPPER_GRATE to Material.WEATHERED_COPPER_GRATE,
			Material.WEATHERED_COPPER_GRATE to Material.OXIDIZED_COPPER_GRATE,

			Material.CUT_COPPER to Material.EXPOSED_CUT_COPPER,
			Material.EXPOSED_CUT_COPPER to Material.WEATHERED_CUT_COPPER,
			Material.WEATHERED_CUT_COPPER to Material.OXIDIZED_CUT_COPPER,

			Material.CUT_COPPER_STAIRS to Material.EXPOSED_CUT_COPPER_STAIRS,
			Material.EXPOSED_CUT_COPPER_STAIRS to Material.WEATHERED_CUT_COPPER_STAIRS,
			Material.WEATHERED_CUT_COPPER_STAIRS to Material.OXIDIZED_CUT_COPPER_STAIRS,

			Material.CUT_COPPER_SLAB to Material.EXPOSED_CUT_COPPER_SLAB,
			Material.EXPOSED_CUT_COPPER_SLAB to Material.WEATHERED_CUT_COPPER_SLAB,
			Material.WEATHERED_CUT_COPPER_SLAB to Material.OXIDIZED_CUT_COPPER_SLAB,

			Material.COPPER_DOOR to Material.EXPOSED_COPPER_DOOR,
			Material.EXPOSED_COPPER_DOOR to Material.WEATHERED_COPPER_DOOR,
			Material.WEATHERED_COPPER_DOOR to Material.OXIDIZED_COPPER_DOOR,

			Material.COPPER_TRAPDOOR to Material.EXPOSED_COPPER_TRAPDOOR,
			Material.EXPOSED_COPPER_TRAPDOOR to Material.WEATHERED_COPPER_TRAPDOOR,
			Material.WEATHERED_COPPER_TRAPDOOR to Material.OXIDIZED_COPPER_TRAPDOOR,

			Material.COPPER_BULB to Material.EXPOSED_COPPER_BULB,
			Material.EXPOSED_COPPER_BULB to Material.WEATHERED_COPPER_BULB,
			Material.WEATHERED_COPPER_BULB to Material.OXIDIZED_COPPER_BULB,
		)

		for ((ingredient, result) in pairs) {
			register(FurnaceMultiblockRecipe(
				identifier = "${ingredient}_OXIDATION",
				clazz = GasFurnaceMultiblock.GasFurnaceMultiblockEntity::class,
				smeltingItem = ItemRequirement.MaterialRequirement(ingredient),
				fuelItem = GasCanisterRequirement(Gasses.OXYGEN, 5),
				power = PowerRequirement(100),
				result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
					duration = Duration.ofSeconds(10),
					normalResult = ItemResult.simpleResult(result),
				))
				.updateFurnace()
			))
		}
	}

	private fun registerAutoMasonRecipes() {
		register(AutoMasonRecipe(
			"TEST",
			ItemRequirement.MaterialRequirement(Material.MELON),
			Material.MELON,
			PowerRequirement(10),
			ResultHolder.of(ItemResult.simpleResult(CustomItemRegistry.ENRICHED_URANIUM))
		))
	}

	fun <E: RecipeEnviornment, R: MultiblockRecipe<E>> register(recipe: R): R {
		recipes.add(recipe)
		byMultiblock[recipe.entityType].add(recipe)

		return recipe
	}

	fun <E: RecipeEnviornment> getRecipesFor(entity: RecipeProcessingMultiblockEntity<E>): Collection<MultiblockRecipe<E>> {
		@Suppress("UNCHECKED_CAST")
		return byMultiblock[entity::class] as Collection<MultiblockRecipe<E>>
	}
}
