package net.horizonsend.ion.server.features.multiblock.crafting

import io.papermc.paper.util.Tick
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.MultiblockRecipeKeys
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.AutoMasonRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.ChemicalProcessorRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.FluidRecipeRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.GridEnergyRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.GasCanisterRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement.MaterialRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.FluidResult
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
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.CenterType
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.SoundCategory
import java.time.Duration
import kotlin.reflect.KClass

class MultiblockRecipeRegistry : Registry<MultiblockRecipe<*>>(RegistryKeys.MULTIBLOCK_RECIPE) {
	override fun getKeySet(): KeyRegistry<MultiblockRecipe<*>> = MultiblockRecipeKeys

	private val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, MultiblockRecipe<*>>()

	override fun registerAdditional(key: IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<*>>, value: MultiblockRecipe<*>) {
		byMultiblock[value.entityType].add(value)
	}

	override fun boostrap() {
		register(MultiblockRecipeKeys.URANIUM_ENRICHMENT, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.URANIUM_ENRICHMENT,
			clazz = CentrifugeMultiblock.CentrifugeMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.URANIUM),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				duration = Duration.ofSeconds(10),
				normalResult = ItemResult.simpleResult(CustomItemKeys.ENRICHED_URANIUM),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.centrifuge"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.URANIUM_CORE_COMPRESSION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.URANIUM_CORE_COMPRESSION,
			clazz = CompressorMultiblock.CompressorMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.URANIUM_CORE),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L),
				ItemResult.simpleResult(CustomItemKeys.URANIUM_ROD),
			))
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.REACTIVE_PLATING_PRESSING, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.REACTIVE_PLATING_PRESSING,
			clazz = PlatePressMultiblock.PlatePressMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.REACTIVE_PLATING),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L),
				ItemResult.simpleResult(CustomItemKeys.REACTIVE_CHASSIS),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.press"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.STEEL_PLATE_PRESSING, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.STEEL_PLATE_PRESSING,
			clazz = PlatePressMultiblock.PlatePressMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.STEEL_PLATE),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L),
				ItemResult.simpleResult(CustomItemKeys.STEEL_CHASSIS),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.press"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.FUEL_ROD_CORE_FABRICATION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.FUEL_ROD_CORE_FABRICATION,
			clazz = FabricatorMultiblock.FabricatorMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.FUEL_ROD_CORE),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L * 2L),
				ItemResult.simpleResult(CustomItemKeys.FUEL_CELL),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.FABRICATED_ASSEMBLY_FABRICATION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.FABRICATED_ASSEMBLY_FABRICATION,
			clazz = FabricatorMultiblock.FabricatorMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.REACTIVE_ASSEMBLY),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L * 2L),
				ItemResult.simpleResult(CustomItemKeys.FABRICATED_ASSEMBLY),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.REINFORCED_FRAME_FABRICATION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.REINFORCED_FRAME_FABRICATION,
			clazz = FabricatorMultiblock.FabricatorMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.STEEL_ASSEMBLY),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L * 2L),
				ItemResult.simpleResult(CustomItemKeys.REINFORCED_FRAME),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.CIRCUIT_BOARD_FABRICATION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.CIRCUIT_BOARD_FABRICATION,
			clazz = CircuitfabMultiblock.CircuitfabMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.CIRCUITRY),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Tick.of(60L * 60L * 20L),
				ItemResult.simpleResult(CustomItemKeys.CIRCUIT_BOARD),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.fabricate"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.LOADED_SHELL_LOADING, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.LOADED_SHELL_LOADING,
			clazz = AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.UNLOADED_SHELL),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Duration.ofSeconds(90),
				ItemResult.simpleResult(CustomItemKeys.LOADED_SHELL),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.load"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.UNCHARGED_SHELL_CHARGING, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.UNCHARGED_SHELL_CHARGING,
			clazz = AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.UNCHARGED_SHELL),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Duration.ofSeconds(90),
				ItemResult.simpleResult(CustomItemKeys.CHARGED_SHELL),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.load"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		register(MultiblockRecipeKeys.ARSENAL_MISSILE_LOADING, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.ARSENAL_MISSILE_LOADING,
			clazz = MissileLoaderMultiblock.MissileLoaderMultiblockEntity::class,
			smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemKeys.UNLOADED_ARSENAL_MISSILE),
			fuelItem = null,
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				Duration.ofMinutes(60),
				ItemResult.simpleResult(CustomItemKeys.ARSENAL_MISSILE),
			))
				.playSound(Sound.sound(NamespacedKeys.packKey("industry.mload"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
				.updateProgressText()
				.updateFurnace()
		))

		registerGasFurnaceRecipes()
		registerAutoMasonRecipes()
		registerChemicalProcessorRecipes()
	}

	private fun registerGasFurnaceRecipes() {
		register(MultiblockRecipeKeys.STEEL_PRODUCTION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.STEEL_PRODUCTION,
			clazz = GasFurnaceMultiblock.GasFurnaceMultiblockEntity::class,
			smeltingItem = MaterialRequirement(Material.IRON_INGOT),
			fuelItem = GasCanisterRequirement(AtmosphericGasKeys.OXYGEN.getValue(), 5),
			power = PowerRequirement(10),
			result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
				duration = Duration.ofSeconds(10),
				normalResult = ItemResult.simpleResult(CustomItemKeys.STEEL_INGOT),
			))
			.updateFurnace()
		))

		val pairs: Array<Triple<IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<FurnaceEnviornment>>, Material, Material>> = arrayOf(
			Triple(MultiblockRecipeKeys.COPPER_BLOCK_OXIDATION, Material.COPPER_BLOCK, Material.EXPOSED_COPPER),
			Triple(MultiblockRecipeKeys.EXPOSED_COPPER_OXIDATION, Material.EXPOSED_COPPER, Material.WEATHERED_COPPER),
			Triple(MultiblockRecipeKeys.WEATHERED_COPPER_OXIDATION, Material.WEATHERED_COPPER, Material.OXIDIZED_COPPER),
			Triple(MultiblockRecipeKeys.CHISELED_COPPER_OXIDATION, Material.CHISELED_COPPER, Material.EXPOSED_CHISELED_COPPER),
			Triple(MultiblockRecipeKeys.EXPOSED_CHISELED_COPPER_OXIDATION, Material.EXPOSED_CHISELED_COPPER, Material.WEATHERED_CHISELED_COPPER),
			Triple(MultiblockRecipeKeys.WEATHERED_CHISELED_COPPER_OXIDATION, Material.WEATHERED_CHISELED_COPPER, Material.OXIDIZED_CHISELED_COPPER),
			Triple(MultiblockRecipeKeys.COPPER_GRATE_OXIDATION, Material.COPPER_GRATE, Material.EXPOSED_COPPER_GRATE),
			Triple(MultiblockRecipeKeys.EXPOSED_COPPER_GRATE_OXIDATION, Material.EXPOSED_COPPER_GRATE, Material.WEATHERED_COPPER_GRATE),
			Triple(MultiblockRecipeKeys.WEATHERED_COPPER_GRATE_OXIDATION, Material.WEATHERED_COPPER_GRATE, Material.OXIDIZED_COPPER_GRATE),
			Triple(MultiblockRecipeKeys.CUT_COPPER_OXIDATION, Material.CUT_COPPER, Material.EXPOSED_CUT_COPPER),
			Triple(MultiblockRecipeKeys.EXPOSED_CUT_COPPER_OXIDATION, Material.EXPOSED_CUT_COPPER, Material.WEATHERED_CUT_COPPER),
			Triple(MultiblockRecipeKeys.WEATHERED_CUT_COPPER_OXIDATION, Material.WEATHERED_CUT_COPPER, Material.OXIDIZED_CUT_COPPER),
			Triple(MultiblockRecipeKeys.CUT_COPPER_STAIRS_OXIDATION, Material.CUT_COPPER_STAIRS, Material.EXPOSED_CUT_COPPER_STAIRS),
			Triple(MultiblockRecipeKeys.EXPOSED_CUT_COPPER_STAIRS_OXIDATION, Material.EXPOSED_CUT_COPPER_STAIRS, Material.WEATHERED_CUT_COPPER_STAIRS),
			Triple(MultiblockRecipeKeys.WEATHERED_CUT_COPPER_STAIRS_OXIDATION, Material.WEATHERED_CUT_COPPER_STAIRS, Material.OXIDIZED_CUT_COPPER_STAIRS),
			Triple(MultiblockRecipeKeys.CUT_COPPER_SLAB_OXIDATION, Material.CUT_COPPER_SLAB, Material.EXPOSED_CUT_COPPER_SLAB),
			Triple(MultiblockRecipeKeys.EXPOSED_CUT_COPPER_SLAB_OXIDATION, Material.EXPOSED_CUT_COPPER_SLAB, Material.WEATHERED_CUT_COPPER_SLAB),
			Triple(MultiblockRecipeKeys.WEATHERED_CUT_COPPER_SLAB_OXIDATION, Material.WEATHERED_CUT_COPPER_SLAB, Material.OXIDIZED_CUT_COPPER_SLAB),
			Triple(MultiblockRecipeKeys.COPPER_DOOR_OXIDATION, Material.COPPER_DOOR, Material.EXPOSED_COPPER_DOOR),
			Triple(MultiblockRecipeKeys.EXPOSED_COPPER_DOOR_OXIDATION, Material.EXPOSED_COPPER_DOOR, Material.WEATHERED_COPPER_DOOR),
			Triple(MultiblockRecipeKeys.WEATHERED_COPPER_DOOR_OXIDATION, Material.WEATHERED_COPPER_DOOR, Material.OXIDIZED_COPPER_DOOR),
			Triple(MultiblockRecipeKeys.COPPER_TRAPDOOR_OXIDATION, Material.COPPER_TRAPDOOR, Material.EXPOSED_COPPER_TRAPDOOR),
			Triple(MultiblockRecipeKeys.EXPOSED_COPPER_TRAPDOOR_OXIDATION, Material.EXPOSED_COPPER_TRAPDOOR, Material.WEATHERED_COPPER_TRAPDOOR),
			Triple(MultiblockRecipeKeys.WEATHERED_COPPER_TRAPDOOR_OXIDATION, Material.WEATHERED_COPPER_TRAPDOOR, Material.OXIDIZED_COPPER_TRAPDOOR),
			Triple(MultiblockRecipeKeys.COPPER_BULB_OXIDATION, Material.COPPER_BULB, Material.EXPOSED_COPPER_BULB),
			Triple(MultiblockRecipeKeys.EXPOSED_COPPER_BULB_OXIDATION, Material.EXPOSED_COPPER_BULB, Material.WEATHERED_COPPER_BULB),
			Triple(MultiblockRecipeKeys.WEATHERED_COPPER_BULB_OXIDATION, Material.WEATHERED_COPPER_BULB, Material.OXIDIZED_COPPER_BULB),
		)

		for ((key, ingredient, result) in pairs) {
			register(key, FurnaceMultiblockRecipe(
				key = key,
				clazz = GasFurnaceMultiblock.GasFurnaceMultiblockEntity::class,
				smeltingItem = MaterialRequirement(ingredient),
				fuelItem = GasCanisterRequirement(AtmosphericGasKeys.OXYGEN.getValue(), 5),
				power = PowerRequirement(100),
				result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
					duration = Duration.ofSeconds(10),
					normalResult = ItemResult.simpleResult(result),
				))
				.updateFurnace()
			))
		}
	}

	internal class MasonRecipe(val key: IonRegistryKey<MultiblockRecipe<*>, AutoMasonRecipe>, val input: Material, val output: Material, val category: CenterType) {
		fun boostrap(registry: MultiblockRecipeRegistry) {
			registry.register(key, AutoMasonRecipe(
				key = key,
				inputItem = MaterialRequirement(input),
				centerCheck = category::matches,
				power = PowerRequirement(10),
				result = ResultHolder.of(ItemResult.simpleResult(output, if (category == CenterType.SLAB) 2 else 1))
			))
		}
	}

	private fun registerAutoMasonRecipes() {
		val recipeMap = listOf<MasonRecipe>(
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ANDESITE_ANDESITE_SLAB_SLAB, input = Material.ANDESITE, output = Material.ANDESITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ANDESITE_ANDESITE_WALL_WALL, input = Material.ANDESITE, output = Material.ANDESITE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ANDESITE_ANDESITE_STAIRS_STAIR, input = Material.ANDESITE, output = Material.ANDESITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_BLACKSTONE_SLAB_SLAB, input = Material.BLACKSTONE, output = Material.BLACKSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_BLACKSTONE_STAIRS_STAIR, input = Material.BLACKSTONE, output = Material.BLACKSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_BLACKSTONE_WALL_WALL, input = Material.BLACKSTONE, output = Material.BLACKSTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BRICKS_BRICK_SLAB_SLAB, input = Material.BRICKS, output = Material.BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BRICKS_BRICK_STAIRS_STAIR, input = Material.BRICKS, output = Material.BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BRICKS_BRICK_WALL_WALL, input = Material.BRICKS, output = Material.BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COPPER_BLOCK_CHISELED_COPPER_CHISELED, input = Material.COPPER_BLOCK, output = Material.CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CUT_COPPER_CHISELED_COPPER_CHISELED, input = Material.CUT_COPPER, output = Material.CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_CHISELED_DEEPSLATE_CHISELED, input = Material.COBBLED_DEEPSLATE, output = Material.CHISELED_DEEPSLATE, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_NETHER_BRICKS_CHISELED_NETHER_BRICKS_CHISELED, input = Material.NETHER_BRICKS, output = Material.CHISELED_NETHER_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_CHISELED_POLISHED_BLACKSTONE_CHISELED, input = Material.BLACKSTONE, output = Material.CHISELED_POLISHED_BLACKSTONE, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_CHISELED_POLISHED_BLACKSTONE_CHISELED, input = Material.POLISHED_BLACKSTONE, output = Material.CHISELED_POLISHED_BLACKSTONE, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_QUARTZ_BLOCK_CHISELED_QUARTZ_BLOCK_CHISELED, input = Material.QUARTZ_BLOCK, output = Material.CHISELED_QUARTZ_BLOCK, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_SANDSTONE_CHISELED_RED_SANDSTONE_CHISELED, input = Material.RED_SANDSTONE, output = Material.CHISELED_RED_SANDSTONE, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RESIN_BRICKS_CHISELED_RESIN_BRICKS_CHISELED, input = Material.RESIN_BRICKS, output = Material.CHISELED_RESIN_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SANDSTONE_CHISELED_SANDSTONE_CHISELED, input = Material.SANDSTONE, output = Material.CHISELED_SANDSTONE, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_BRICKS_CHISELED_STONE_BRICKS_CHISELED, input = Material.STONE_BRICKS, output = Material.CHISELED_STONE_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_CHISELED_STONE_BRICKS_CHISELED, input = Material.STONE, output = Material.CHISELED_STONE_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_CHISELED_TUFF_BRICKS_CHISELED, input = Material.POLISHED_TUFF, output = Material.CHISELED_TUFF_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_BRICKS_CHISELED_TUFF_BRICKS_CHISELED, input = Material.TUFF_BRICKS, output = Material.CHISELED_TUFF_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_CHISELED_TUFF_BRICKS_CHISELED, input = Material.TUFF, output = Material.CHISELED_TUFF_BRICKS, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_CHISELED_TUFF_CHISELED, input = Material.TUFF, output = Material.CHISELED_TUFF, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_COBBLED_DEEPSLATE_SLAB_SLAB, input = Material.COBBLED_DEEPSLATE, output = Material.COBBLED_DEEPSLATE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_COBBLED_DEEPSLATE_STAIRS_STAIR, input = Material.COBBLED_DEEPSLATE, output = Material.COBBLED_DEEPSLATE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_COBBLED_DEEPSLATE_WALL_WALL, input = Material.COBBLED_DEEPSLATE, output = Material.COBBLED_DEEPSLATE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLESTONE_COBBLESTONE_SLAB_SLAB, input = Material.COBBLESTONE, output = Material.COBBLESTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLESTONE_COBBLESTONE_STAIRS_STAIR, input = Material.COBBLESTONE, output = Material.COBBLESTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLESTONE_COBBLESTONE_WALL_WALL, input = Material.COBBLESTONE, output = Material.COBBLESTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COPPER_BLOCK_COPPER_GRATE_GRATE, input = Material.COPPER_BLOCK, output = Material.COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COPPER_BLOCK_CUT_COPPER_CUT, input = Material.COPPER_BLOCK, output = Material.CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COPPER_BLOCK_CUT_COPPER_SLAB_SLAB, input = Material.COPPER_BLOCK, output = Material.CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CUT_COPPER_CUT_COPPER_SLAB_SLAB, input = Material.CUT_COPPER, output = Material.CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COPPER_BLOCK_CUT_COPPER_STAIRS_STAIR, input = Material.COPPER_BLOCK, output = Material.CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CUT_COPPER_CUT_COPPER_STAIRS_STAIR, input = Material.CUT_COPPER, output = Material.CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_SANDSTONE_CUT_RED_SANDSTONE_CUT, input = Material.RED_SANDSTONE, output = Material.CUT_RED_SANDSTONE, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CUT_RED_SANDSTONE_CUT_RED_SANDSTONE_SLAB_SLAB, input = Material.CUT_RED_SANDSTONE, output = Material.CUT_RED_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_SANDSTONE_CUT_RED_SANDSTONE_SLAB_SLAB, input = Material.RED_SANDSTONE, output = Material.CUT_RED_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SANDSTONE_CUT_SANDSTONE_CUT, input = Material.SANDSTONE, output = Material.CUT_SANDSTONE, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CUT_SANDSTONE_CUT_SANDSTONE_SLAB_SLAB, input = Material.CUT_SANDSTONE, output = Material.CUT_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SANDSTONE_CUT_SANDSTONE_SLAB_SLAB, input = Material.SANDSTONE, output = Material.CUT_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DARK_PRISMARINE_DARK_PRISMARINE_SLAB_SLAB, input = Material.DARK_PRISMARINE, output = Material.DARK_PRISMARINE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DARK_PRISMARINE_DARK_PRISMARINE_STAIRS_STAIR, input = Material.DARK_PRISMARINE, output = Material.DARK_PRISMARINE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_BRICK_SLAB_SLAB, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_BRICK_SLAB_SLAB, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_BRICK_SLAB_SLAB, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_BRICK_STAIRS_STAIR, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_BRICK_STAIRS_STAIR, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_BRICK_STAIRS_STAIR, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_BRICK_WALL_WALL, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_BRICK_WALL_WALL, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_BRICK_WALL_WALL, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_BRICKS_BRICKS, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_BRICKS_BRICKS, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_TILE_SLAB_SLAB, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_TILE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_TILE_SLAB_SLAB, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_TILE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_TILES_DEEPSLATE_TILE_SLAB_SLAB, input = Material.DEEPSLATE_TILES, output = Material.DEEPSLATE_TILE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_TILE_SLAB_SLAB, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_TILE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_TILE_STAIRS_STAIR, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_TILE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_TILE_STAIRS_STAIR, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_TILE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_TILES_DEEPSLATE_TILE_STAIRS_STAIR, input = Material.DEEPSLATE_TILES, output = Material.DEEPSLATE_TILE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_TILE_STAIRS_STAIR, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_TILE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_TILE_WALL_WALL, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_TILE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_TILE_WALL_WALL, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_TILE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_TILES_DEEPSLATE_TILE_WALL_WALL, input = Material.DEEPSLATE_TILES, output = Material.DEEPSLATE_TILE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_TILE_WALL_WALL, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_TILE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_DEEPSLATE_TILES_TILES, input = Material.COBBLED_DEEPSLATE, output = Material.DEEPSLATE_TILES, category = CenterType.TILES),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DEEPSLATE_BRICKS_DEEPSLATE_TILES_TILES, input = Material.DEEPSLATE_BRICKS, output = Material.DEEPSLATE_TILES, category = CenterType.TILES),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_DEEPSLATE_TILES_TILES, input = Material.POLISHED_DEEPSLATE, output = Material.DEEPSLATE_TILES, category = CenterType.TILES),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DIORITE_DIORITE_SLAB_SLAB, input = Material.DIORITE, output = Material.DIORITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DIORITE_DIORITE_STAIRS_STAIR, input = Material.DIORITE, output = Material.DIORITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DIORITE_DIORITE_WALL_WALL, input = Material.DIORITE, output = Material.DIORITE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_BRICKS_END_STONE_BRICK_SLAB_SLAB, input = Material.END_STONE_BRICKS, output = Material.END_STONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_END_STONE_BRICK_SLAB_SLAB, input = Material.END_STONE, output = Material.END_STONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_BRICKS_END_STONE_BRICK_STAIRS_STAIR, input = Material.END_STONE_BRICKS, output = Material.END_STONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_END_STONE_BRICK_STAIRS_STAIR, input = Material.END_STONE, output = Material.END_STONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_BRICKS_END_STONE_BRICK_WALL_WALL, input = Material.END_STONE_BRICKS, output = Material.END_STONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_END_STONE_BRICK_WALL_WALL, input = Material.END_STONE, output = Material.END_STONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_END_STONE_END_STONE_BRICKS_BRICKS, input = Material.END_STONE, output = Material.END_STONE_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_COPPER_EXPOSED_CHISELED_COPPER_CHISELED, input = Material.EXPOSED_COPPER, output = Material.EXPOSED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_CUT_COPPER_EXPOSED_CHISELED_COPPER_CHISELED, input = Material.EXPOSED_CUT_COPPER, output = Material.EXPOSED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_COPPER_EXPOSED_COPPER_GRATE_GRATE, input = Material.EXPOSED_COPPER, output = Material.EXPOSED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_COPPER_EXPOSED_CUT_COPPER_CUT, input = Material.EXPOSED_COPPER, output = Material.EXPOSED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_COPPER_EXPOSED_CUT_COPPER_SLAB_SLAB, input = Material.EXPOSED_COPPER, output = Material.EXPOSED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_CUT_COPPER_EXPOSED_CUT_COPPER_SLAB_SLAB, input = Material.EXPOSED_CUT_COPPER, output = Material.EXPOSED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_COPPER_EXPOSED_CUT_COPPER_STAIRS_STAIR, input = Material.EXPOSED_COPPER, output = Material.EXPOSED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_EXPOSED_CUT_COPPER_EXPOSED_CUT_COPPER_STAIRS_STAIR, input = Material.EXPOSED_CUT_COPPER, output = Material.EXPOSED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_GRANITE_GRANITE_SLAB_SLAB, input = Material.GRANITE, output = Material.GRANITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_GRANITE_GRANITE_STAIRS_STAIR, input = Material.GRANITE, output = Material.GRANITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_GRANITE_GRANITE_WALL_WALL, input = Material.GRANITE, output = Material.GRANITE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MOSSY_COBBLESTONE_MOSSY_COBBLESTONE_SLAB_SLAB, input = Material.MOSSY_COBBLESTONE, output = Material.MOSSY_COBBLESTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MOSSY_COBBLESTONE_MOSSY_COBBLESTONE_STAIRS_STAIR, input = Material.MOSSY_COBBLESTONE, output = Material.MOSSY_COBBLESTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MOSSY_COBBLESTONE_MOSSY_COBBLESTONE_WALL_WALL, input = Material.MOSSY_COBBLESTONE, output = Material.MOSSY_COBBLESTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MOSSY_STONE_BRICKS_MOSSY_STONE_BRICK_SLAB_SLAB, input = Material.MOSSY_STONE_BRICKS, output = Material.MOSSY_STONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MOSSY_STONE_BRICKS_MOSSY_STONE_BRICK_STAIRS_STAIR, input = Material.MOSSY_STONE_BRICKS, output = Material.MOSSY_STONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MOSSY_STONE_BRICKS_MOSSY_STONE_BRICK_WALL_WALL, input = Material.MOSSY_STONE_BRICKS, output = Material.MOSSY_STONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MUD_BRICKS_MUD_BRICK_SLAB_SLAB, input = Material.MUD_BRICKS, output = Material.MUD_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MUD_BRICKS_MUD_BRICK_STAIRS_STAIR, input = Material.MUD_BRICKS, output = Material.MUD_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MUD_BRICKS_MUD_BRICK_WALL_WALL, input = Material.MUD_BRICKS, output = Material.MUD_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_NETHER_BRICKS_NETHER_BRICK_SLAB_SLAB, input = Material.NETHER_BRICKS, output = Material.NETHER_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_NETHER_BRICKS_NETHER_BRICK_STAIRS_STAIR, input = Material.NETHER_BRICKS, output = Material.NETHER_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_NETHER_BRICKS_NETHER_BRICK_WALL_WALL, input = Material.NETHER_BRICKS, output = Material.NETHER_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_COPPER_OXIDIZED_CHISELED_COPPER_CHISELED, input = Material.OXIDIZED_COPPER, output = Material.OXIDIZED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_CUT_COPPER_OXIDIZED_CHISELED_COPPER_CHISELED, input = Material.OXIDIZED_CUT_COPPER, output = Material.OXIDIZED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_COPPER_OXIDIZED_COPPER_GRATE_GRATE, input = Material.OXIDIZED_COPPER, output = Material.OXIDIZED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_COPPER_OXIDIZED_CUT_COPPER_CUT, input = Material.OXIDIZED_COPPER, output = Material.OXIDIZED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_COPPER_OXIDIZED_CUT_COPPER_SLAB_SLAB, input = Material.OXIDIZED_COPPER, output = Material.OXIDIZED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_CUT_COPPER_OXIDIZED_CUT_COPPER_SLAB_SLAB, input = Material.OXIDIZED_CUT_COPPER, output = Material.OXIDIZED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_COPPER_OXIDIZED_CUT_COPPER_STAIRS_STAIR, input = Material.OXIDIZED_COPPER, output = Material.OXIDIZED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OXIDIZED_CUT_COPPER_OXIDIZED_CUT_COPPER_STAIRS_STAIR, input = Material.OXIDIZED_CUT_COPPER, output = Material.OXIDIZED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ANDESITE_POLISHED_ANDESITE_POLISHED, input = Material.ANDESITE, output = Material.POLISHED_ANDESITE, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ANDESITE_POLISHED_ANDESITE_SLAB_SLAB, input = Material.ANDESITE, output = Material.POLISHED_ANDESITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_ANDESITE_POLISHED_ANDESITE_SLAB_SLAB, input = Material.POLISHED_ANDESITE, output = Material.POLISHED_ANDESITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ANDESITE_POLISHED_ANDESITE_STAIRS_STAIR, input = Material.ANDESITE, output = Material.POLISHED_ANDESITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_ANDESITE_POLISHED_ANDESITE_STAIRS_STAIR, input = Material.POLISHED_ANDESITE, output = Material.POLISHED_ANDESITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BASALT_POLISHED_BASALT_POLISHED, input = Material.BASALT, output = Material.POLISHED_BASALT, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_BRICK_SLAB_SLAB, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_BRICKS_POLISHED_BLACKSTONE_BRICK_SLAB_SLAB, input = Material.POLISHED_BLACKSTONE_BRICKS, output = Material.POLISHED_BLACKSTONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_BRICK_SLAB_SLAB, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_BRICK_STAIRS_STAIR, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_BRICKS_POLISHED_BLACKSTONE_BRICK_STAIRS_STAIR, input = Material.POLISHED_BLACKSTONE_BRICKS, output = Material.POLISHED_BLACKSTONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_BRICK_STAIRS_STAIR, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_BRICK_WALL_WALL, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_BRICKS_POLISHED_BLACKSTONE_BRICK_WALL_WALL, input = Material.POLISHED_BLACKSTONE_BRICKS, output = Material.POLISHED_BLACKSTONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_BRICK_WALL_WALL, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_BRICKS_BRICKS, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_BRICKS_BRICKS, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_POLISHED, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_SLAB_SLAB, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_SLAB_SLAB, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_STAIRS_STAIR, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_STAIRS_STAIR, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BLACKSTONE_POLISHED_BLACKSTONE_WALL_WALL, input = Material.BLACKSTONE, output = Material.POLISHED_BLACKSTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_BLACKSTONE_POLISHED_BLACKSTONE_WALL_WALL, input = Material.POLISHED_BLACKSTONE, output = Material.POLISHED_BLACKSTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_POLISHED_DEEPSLATE_POLISHED, input = Material.COBBLED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_POLISHED_DEEPSLATE_SLAB_SLAB, input = Material.COBBLED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_POLISHED_DEEPSLATE_SLAB_SLAB, input = Material.POLISHED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_POLISHED_DEEPSLATE_STAIRS_STAIR, input = Material.COBBLED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_POLISHED_DEEPSLATE_STAIRS_STAIR, input = Material.POLISHED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_COBBLED_DEEPSLATE_POLISHED_DEEPSLATE_WALL_WALL, input = Material.COBBLED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DEEPSLATE_POLISHED_DEEPSLATE_WALL_WALL, input = Material.POLISHED_DEEPSLATE, output = Material.POLISHED_DEEPSLATE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DIORITE_POLISHED_DIORITE_POLISHED, input = Material.DIORITE, output = Material.POLISHED_DIORITE, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DIORITE_POLISHED_DIORITE_SLAB_SLAB, input = Material.DIORITE, output = Material.POLISHED_DIORITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DIORITE_POLISHED_DIORITE_SLAB_SLAB, input = Material.POLISHED_DIORITE, output = Material.POLISHED_DIORITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DIORITE_POLISHED_DIORITE_STAIRS_STAIR, input = Material.DIORITE, output = Material.POLISHED_DIORITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_DIORITE_POLISHED_DIORITE_STAIRS_STAIR, input = Material.POLISHED_DIORITE, output = Material.POLISHED_DIORITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_GRANITE_POLISHED_GRANITE_POLISHED, input = Material.GRANITE, output = Material.POLISHED_GRANITE, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_GRANITE_POLISHED_GRANITE_SLAB_SLAB, input = Material.GRANITE, output = Material.POLISHED_GRANITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_GRANITE_POLISHED_GRANITE_SLAB_SLAB, input = Material.POLISHED_GRANITE, output = Material.POLISHED_GRANITE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_GRANITE_POLISHED_GRANITE_STAIRS_STAIR, input = Material.GRANITE, output = Material.POLISHED_GRANITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_GRANITE_POLISHED_GRANITE_STAIRS_STAIR, input = Material.POLISHED_GRANITE, output = Material.POLISHED_GRANITE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_POLISHED_TUFF_POLISHED, input = Material.TUFF, output = Material.POLISHED_TUFF, category = CenterType.POLISHED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_POLISHED_TUFF_SLAB_SLAB, input = Material.POLISHED_TUFF, output = Material.POLISHED_TUFF_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_POLISHED_TUFF_SLAB_SLAB, input = Material.TUFF, output = Material.POLISHED_TUFF_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_POLISHED_TUFF_STAIRS_STAIR, input = Material.POLISHED_TUFF, output = Material.POLISHED_TUFF_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_POLISHED_TUFF_STAIRS_STAIR, input = Material.TUFF, output = Material.POLISHED_TUFF_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_POLISHED_TUFF_WALL_WALL, input = Material.POLISHED_TUFF, output = Material.POLISHED_TUFF_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_POLISHED_TUFF_WALL_WALL, input = Material.TUFF, output = Material.POLISHED_TUFF_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PRISMARINE_BRICKS_PRISMARINE_BRICK_SLAB_SLAB, input = Material.PRISMARINE_BRICKS, output = Material.PRISMARINE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PRISMARINE_BRICKS_PRISMARINE_BRICK_STAIRS_STAIR, input = Material.PRISMARINE_BRICKS, output = Material.PRISMARINE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PRISMARINE_PRISMARINE_SLAB_SLAB, input = Material.PRISMARINE, output = Material.PRISMARINE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PRISMARINE_PRISMARINE_STAIRS_STAIR, input = Material.PRISMARINE, output = Material.PRISMARINE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PRISMARINE_PRISMARINE_WALL_WALL, input = Material.PRISMARINE, output = Material.PRISMARINE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PURPUR_BLOCK_PURPUR_PILLAR_PILLAR, input = Material.PURPUR_BLOCK, output = Material.PURPUR_PILLAR, category = CenterType.PILLAR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PURPUR_BLOCK_PURPUR_SLAB_SLAB, input = Material.PURPUR_BLOCK, output = Material.PURPUR_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PURPUR_BLOCK_PURPUR_STAIRS_STAIR, input = Material.PURPUR_BLOCK, output = Material.PURPUR_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_QUARTZ_BLOCK_QUARTZ_BRICKS_BRICKS, input = Material.QUARTZ_BLOCK, output = Material.QUARTZ_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_QUARTZ_BLOCK_QUARTZ_PILLAR_PILLAR, input = Material.QUARTZ_BLOCK, output = Material.QUARTZ_PILLAR, category = CenterType.PILLAR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_QUARTZ_BLOCK_QUARTZ_SLAB_SLAB, input = Material.QUARTZ_BLOCK, output = Material.QUARTZ_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_QUARTZ_BLOCK_QUARTZ_STAIRS_STAIR, input = Material.QUARTZ_BLOCK, output = Material.QUARTZ_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_NETHER_BRICKS_RED_NETHER_BRICK_SLAB_SLAB, input = Material.RED_NETHER_BRICKS, output = Material.RED_NETHER_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_NETHER_BRICKS_RED_NETHER_BRICK_STAIRS_STAIR, input = Material.RED_NETHER_BRICKS, output = Material.RED_NETHER_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_NETHER_BRICKS_RED_NETHER_BRICK_WALL_WALL, input = Material.RED_NETHER_BRICKS, output = Material.RED_NETHER_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_SANDSTONE_RED_SANDSTONE_SLAB_SLAB, input = Material.RED_SANDSTONE, output = Material.RED_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_SANDSTONE_RED_SANDSTONE_STAIRS_STAIR, input = Material.RED_SANDSTONE, output = Material.RED_SANDSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RED_SANDSTONE_RED_SANDSTONE_WALL_WALL, input = Material.RED_SANDSTONE, output = Material.RED_SANDSTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RESIN_BRICKS_RESIN_BRICK_SLAB_SLAB, input = Material.RESIN_BRICKS, output = Material.RESIN_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RESIN_BRICKS_RESIN_BRICK_STAIRS_STAIR, input = Material.RESIN_BRICKS, output = Material.RESIN_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_RESIN_BRICKS_RESIN_BRICK_WALL_WALL, input = Material.RESIN_BRICKS, output = Material.RESIN_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SANDSTONE_SANDSTONE_SLAB_SLAB, input = Material.SANDSTONE, output = Material.SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SANDSTONE_SANDSTONE_STAIRS_STAIR, input = Material.SANDSTONE, output = Material.SANDSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SANDSTONE_SANDSTONE_WALL_WALL, input = Material.SANDSTONE, output = Material.SANDSTONE_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_QUARTZ_SMOOTH_QUARTZ_SLAB_SLAB, input = Material.SMOOTH_QUARTZ, output = Material.SMOOTH_QUARTZ_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_QUARTZ_SMOOTH_QUARTZ_STAIRS_STAIR, input = Material.SMOOTH_QUARTZ, output = Material.SMOOTH_QUARTZ_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_RED_SANDSTONE_SMOOTH_RED_SANDSTONE_SLAB_SLAB, input = Material.SMOOTH_RED_SANDSTONE, output = Material.SMOOTH_RED_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_RED_SANDSTONE_SMOOTH_RED_SANDSTONE_STAIRS_STAIR, input = Material.SMOOTH_RED_SANDSTONE, output = Material.SMOOTH_RED_SANDSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_SANDSTONE_SMOOTH_SANDSTONE_SLAB_SLAB, input = Material.SMOOTH_SANDSTONE, output = Material.SMOOTH_SANDSTONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_SANDSTONE_SMOOTH_SANDSTONE_STAIRS_STAIR, input = Material.SMOOTH_SANDSTONE, output = Material.SMOOTH_SANDSTONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SMOOTH_STONE_SMOOTH_STONE_SLAB_SLAB, input = Material.SMOOTH_STONE, output = Material.SMOOTH_STONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_BRICKS_STONE_BRICK_SLAB_SLAB, input = Material.STONE_BRICKS, output = Material.STONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_STONE_BRICK_SLAB_SLAB, input = Material.STONE, output = Material.STONE_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_BRICKS_STONE_BRICK_STAIRS_STAIR, input = Material.STONE_BRICKS, output = Material.STONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_STONE_BRICK_STAIRS_STAIR, input = Material.STONE, output = Material.STONE_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_BRICKS_STONE_BRICK_WALL_WALL, input = Material.STONE_BRICKS, output = Material.STONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_STONE_BRICK_WALL_WALL, input = Material.STONE, output = Material.STONE_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_STONE_BRICKS_BRICKS, input = Material.STONE, output = Material.STONE_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_STONE_SLAB_SLAB, input = Material.STONE, output = Material.STONE_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STONE_STONE_STAIRS_STAIR, input = Material.STONE, output = Material.STONE_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_TUFF_BRICK_SLAB_SLAB, input = Material.POLISHED_TUFF, output = Material.TUFF_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_BRICKS_TUFF_BRICK_SLAB_SLAB, input = Material.TUFF_BRICKS, output = Material.TUFF_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_BRICK_SLAB_SLAB, input = Material.TUFF, output = Material.TUFF_BRICK_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_TUFF_BRICK_STAIRS_STAIR, input = Material.POLISHED_TUFF, output = Material.TUFF_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_BRICKS_TUFF_BRICK_STAIRS_STAIR, input = Material.TUFF_BRICKS, output = Material.TUFF_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_BRICK_STAIRS_STAIR, input = Material.TUFF, output = Material.TUFF_BRICK_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_TUFF_BRICK_WALL_WALL, input = Material.POLISHED_TUFF, output = Material.TUFF_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_BRICKS_TUFF_BRICK_WALL_WALL, input = Material.TUFF_BRICKS, output = Material.TUFF_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_BRICK_WALL_WALL, input = Material.TUFF, output = Material.TUFF_BRICK_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_POLISHED_TUFF_TUFF_BRICKS_BRICKS, input = Material.POLISHED_TUFF, output = Material.TUFF_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_BRICKS_BRICKS, input = Material.TUFF, output = Material.TUFF_BRICKS, category = CenterType.BRICKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_SLAB_SLAB, input = Material.TUFF, output = Material.TUFF_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_STAIRS_STAIR, input = Material.TUFF, output = Material.TUFF_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_TUFF_TUFF_WALL_WALL, input = Material.TUFF, output = Material.TUFF_WALL, category = CenterType.WALL),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_COPPER_BLOCK_WAXED_CHISELED_COPPER_CHISELED, input = Material.WAXED_COPPER_BLOCK, output = Material.WAXED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_CUT_COPPER_WAXED_CHISELED_COPPER_CHISELED, input = Material.WAXED_CUT_COPPER, output = Material.WAXED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_COPPER_BLOCK_WAXED_COPPER_GRATE_GRATE, input = Material.WAXED_COPPER_BLOCK, output = Material.WAXED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_COPPER_BLOCK_WAXED_CUT_COPPER_CUT, input = Material.WAXED_COPPER_BLOCK, output = Material.WAXED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_COPPER_BLOCK_WAXED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_COPPER_BLOCK, output = Material.WAXED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_CUT_COPPER_WAXED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_CUT_COPPER, output = Material.WAXED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_COPPER_BLOCK_WAXED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_COPPER_BLOCK, output = Material.WAXED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_CUT_COPPER_WAXED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_CUT_COPPER, output = Material.WAXED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_COPPER_WAXED_EXPOSED_CHISELED_COPPER_CHISELED, input = Material.WAXED_EXPOSED_COPPER, output = Material.WAXED_EXPOSED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_CUT_COPPER_WAXED_EXPOSED_CHISELED_COPPER_CHISELED, input = Material.WAXED_EXPOSED_CUT_COPPER, output = Material.WAXED_EXPOSED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_COPPER_WAXED_EXPOSED_COPPER_GRATE_GRATE, input = Material.WAXED_EXPOSED_COPPER, output = Material.WAXED_EXPOSED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_COPPER_WAXED_EXPOSED_CUT_COPPER_CUT, input = Material.WAXED_EXPOSED_COPPER, output = Material.WAXED_EXPOSED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_COPPER_WAXED_EXPOSED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_EXPOSED_COPPER, output = Material.WAXED_EXPOSED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_CUT_COPPER_WAXED_EXPOSED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_EXPOSED_CUT_COPPER, output = Material.WAXED_EXPOSED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_COPPER_WAXED_EXPOSED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_EXPOSED_COPPER, output = Material.WAXED_EXPOSED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_EXPOSED_CUT_COPPER_WAXED_EXPOSED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_EXPOSED_CUT_COPPER, output = Material.WAXED_EXPOSED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_COPPER_WAXED_OXIDIZED_CHISELED_COPPER_CHISELED, input = Material.WAXED_OXIDIZED_COPPER, output = Material.WAXED_OXIDIZED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_CUT_COPPER_WAXED_OXIDIZED_CHISELED_COPPER_CHISELED, input = Material.WAXED_OXIDIZED_CUT_COPPER, output = Material.WAXED_OXIDIZED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_COPPER_WAXED_OXIDIZED_COPPER_GRATE_GRATE, input = Material.WAXED_OXIDIZED_COPPER, output = Material.WAXED_OXIDIZED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_COPPER_WAXED_OXIDIZED_CUT_COPPER_CUT, input = Material.WAXED_OXIDIZED_COPPER, output = Material.WAXED_OXIDIZED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_COPPER_WAXED_OXIDIZED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_OXIDIZED_COPPER, output = Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_CUT_COPPER_WAXED_OXIDIZED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_OXIDIZED_CUT_COPPER, output = Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_COPPER_WAXED_OXIDIZED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_OXIDIZED_COPPER, output = Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_OXIDIZED_CUT_COPPER_WAXED_OXIDIZED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_OXIDIZED_CUT_COPPER, output = Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_COPPER_WAXED_WEATHERED_CHISELED_COPPER_CHISELED, input = Material.WAXED_WEATHERED_COPPER, output = Material.WAXED_WEATHERED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_CUT_COPPER_WAXED_WEATHERED_CHISELED_COPPER_CHISELED, input = Material.WAXED_WEATHERED_CUT_COPPER, output = Material.WAXED_WEATHERED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_COPPER_WAXED_WEATHERED_COPPER_GRATE_GRATE, input = Material.WAXED_WEATHERED_COPPER, output = Material.WAXED_WEATHERED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_COPPER_WAXED_WEATHERED_CUT_COPPER_CUT, input = Material.WAXED_WEATHERED_COPPER, output = Material.WAXED_WEATHERED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_COPPER_WAXED_WEATHERED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_WEATHERED_COPPER, output = Material.WAXED_WEATHERED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_CUT_COPPER_WAXED_WEATHERED_CUT_COPPER_SLAB_SLAB, input = Material.WAXED_WEATHERED_CUT_COPPER, output = Material.WAXED_WEATHERED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_COPPER_WAXED_WEATHERED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_WEATHERED_COPPER, output = Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WAXED_WEATHERED_CUT_COPPER_WAXED_WEATHERED_CUT_COPPER_STAIRS_STAIR, input = Material.WAXED_WEATHERED_CUT_COPPER, output = Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_COPPER_WEATHERED_CHISELED_COPPER_CHISELED, input = Material.WEATHERED_COPPER, output = Material.WEATHERED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_CUT_COPPER_WEATHERED_CHISELED_COPPER_CHISELED, input = Material.WEATHERED_CUT_COPPER, output = Material.WEATHERED_CHISELED_COPPER, category = CenterType.CHISELED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_COPPER_WEATHERED_COPPER_GRATE_GRATE, input = Material.WEATHERED_COPPER, output = Material.WEATHERED_COPPER_GRATE, category = CenterType.GRATE),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_COPPER_WEATHERED_CUT_COPPER_CUT, input = Material.WEATHERED_COPPER, output = Material.WEATHERED_CUT_COPPER, category = CenterType.CUT),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_COPPER_WEATHERED_CUT_COPPER_SLAB_SLAB, input = Material.WEATHERED_COPPER, output = Material.WEATHERED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_CUT_COPPER_WEATHERED_CUT_COPPER_SLAB_SLAB, input = Material.WEATHERED_CUT_COPPER, output = Material.WEATHERED_CUT_COPPER_SLAB, category = CenterType.SLAB),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_COPPER_WEATHERED_CUT_COPPER_STAIRS_STAIR, input = Material.WEATHERED_COPPER, output = Material.WEATHERED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_WEATHERED_CUT_COPPER_WEATHERED_CUT_COPPER_STAIRS_STAIR, input = Material.WEATHERED_CUT_COPPER, output = Material.WEATHERED_CUT_COPPER_STAIRS, category = CenterType.STAIR),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OAK_LOG_OAK_PLANKS_PLANKS, input = Material.OAK_LOG, output = Material.OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SPRUCE_LOG_SPRUCE_PLANKS_PLANKS, input = Material.SPRUCE_LOG, output = Material.SPRUCE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BIRCH_LOG_BIRCH_PLANKS_PLANKS, input = Material.BIRCH_LOG, output = Material.BIRCH_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_JUNGLE_LOG_JUNGLE_PLANKS_PLANKS, input = Material.JUNGLE_LOG, output = Material.JUNGLE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ACACIA_LOG_ACACIA_PLANKS_PLANKS, input = Material.ACACIA_LOG, output = Material.ACACIA_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CHERRY_LOG_CHERRY_PLANKS_PLANKS, input = Material.CHERRY_LOG, output = Material.CHERRY_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PALE_OAK_LOG_PALE_OAK_PLANKS_PLANKS, input = Material.PALE_OAK_LOG, output = Material.PALE_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DARK_OAK_LOG_DARK_OAK_PLANKS_PLANKS, input = Material.DARK_OAK_LOG, output = Material.DARK_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MANGROVE_LOG_MANGROVE_PLANKS_PLANKS, input = Material.MANGROVE_LOG, output = Material.MANGROVE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OAK_WOOD_OAK_PLANKS_PLANKS, input = Material.OAK_WOOD, output = Material.OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SPRUCE_WOOD_SPRUCE_PLANKS_PLANKS, input = Material.SPRUCE_WOOD, output = Material.SPRUCE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BIRCH_WOOD_BIRCH_PLANKS_PLANKS, input = Material.BIRCH_WOOD, output = Material.BIRCH_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_JUNGLE_WOOD_JUNGLE_PLANKS_PLANKS, input = Material.JUNGLE_WOOD, output = Material.JUNGLE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ACACIA_WOOD_ACACIA_PLANKS_PLANKS, input = Material.ACACIA_WOOD, output = Material.ACACIA_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CHERRY_WOOD_CHERRY_PLANKS_PLANKS, input = Material.CHERRY_WOOD, output = Material.CHERRY_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PALE_OAK_WOOD_PALE_OAK_PLANKS_PLANKS, input = Material.PALE_OAK_WOOD, output = Material.PALE_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DARK_OAK_WOOD_DARK_OAK_PLANKS_PLANKS, input = Material.DARK_OAK_WOOD, output = Material.DARK_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MANGROVE_WOOD_MANGROVE_PLANKS_PLANKS, input = Material.MANGROVE_WOOD, output = Material.MANGROVE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_OAK_LOG_OAK_PLANKS_PLANKS, input = Material.STRIPPED_OAK_LOG, output = Material.OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_SPRUCE_LOG_SPRUCE_PLANKS_PLANKS, input = Material.STRIPPED_SPRUCE_LOG, output = Material.SPRUCE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_BIRCH_LOG_BIRCH_PLANKS_PLANKS, input = Material.STRIPPED_BIRCH_LOG, output = Material.BIRCH_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_JUNGLE_LOG_JUNGLE_PLANKS_PLANKS, input = Material.STRIPPED_JUNGLE_LOG, output = Material.JUNGLE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_ACACIA_LOG_ACACIA_PLANKS_PLANKS, input = Material.STRIPPED_ACACIA_LOG, output = Material.ACACIA_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_CHERRY_LOG_CHERRY_PLANKS_PLANKS, input = Material.STRIPPED_CHERRY_LOG, output = Material.CHERRY_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_DARK_OAK_LOG_DARK_OAK_PLANKS_PLANKS, input = Material.STRIPPED_DARK_OAK_LOG, output = Material.DARK_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_PALE_OAK_LOG_PALE_OAK_PLANKS_PLANKS, input = Material.STRIPPED_PALE_OAK_LOG, output = Material.PALE_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_MANGROVE_LOG_MANGROVE_PLANKS_PLANKS, input = Material.STRIPPED_MANGROVE_LOG, output = Material.MANGROVE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_OAK_WOOD_OAK_PLANKS_PLANKS, input = Material.STRIPPED_OAK_WOOD, output = Material.OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_SPRUCE_WOOD_SPRUCE_PLANKS_PLANKS, input = Material.STRIPPED_SPRUCE_WOOD, output = Material.SPRUCE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_BIRCH_WOOD_BIRCH_PLANKS_PLANKS, input = Material.STRIPPED_BIRCH_WOOD, output = Material.BIRCH_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_JUNGLE_WOOD_JUNGLE_PLANKS_PLANKS, input = Material.STRIPPED_JUNGLE_WOOD, output = Material.JUNGLE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_ACACIA_WOOD_ACACIA_PLANKS_PLANKS, input = Material.STRIPPED_ACACIA_WOOD, output = Material.ACACIA_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_CHERRY_WOOD_CHERRY_PLANKS_PLANKS, input = Material.STRIPPED_CHERRY_WOOD, output = Material.CHERRY_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_DARK_OAK_WOOD_DARK_OAK_PLANKS_PLANKS, input = Material.STRIPPED_DARK_OAK_WOOD, output = Material.DARK_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_PALE_OAK_WOOD_PALE_OAK_PLANKS_PLANKS, input = Material.STRIPPED_PALE_OAK_WOOD, output = Material.PALE_OAK_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_STRIPPED_MANGROVE_WOOD_MANGROVE_PLANKS_PLANKS, input = Material.STRIPPED_MANGROVE_WOOD, output = Material.MANGROVE_PLANKS, category = CenterType.PLANKS),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OAK_LOG_STRIPPED_OAK_LOG_STRIPPED, input = Material.OAK_LOG, output = Material.STRIPPED_OAK_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SPRUCE_LOG_STRIPPED_SPRUCE_LOG_STRIPPED, input = Material.SPRUCE_LOG, output = Material.STRIPPED_SPRUCE_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BIRCH_LOG_STRIPPED_BIRCH_LOG_STRIPPED, input = Material.BIRCH_LOG, output = Material.STRIPPED_BIRCH_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_JUNGLE_LOG_STRIPPED_JUNGLE_LOG_STRIPPED, input = Material.JUNGLE_LOG, output = Material.STRIPPED_JUNGLE_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ACACIA_LOG_STRIPPED_ACACIA_LOG_STRIPPED, input = Material.ACACIA_LOG, output = Material.STRIPPED_ACACIA_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CHERRY_LOG_STRIPPED_CHERRY_LOG_STRIPPED, input = Material.CHERRY_LOG, output = Material.STRIPPED_CHERRY_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PALE_OAK_LOG_STRIPPED_PALE_OAK_LOG_STRIPPED, input = Material.PALE_OAK_LOG, output = Material.STRIPPED_PALE_OAK_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DARK_OAK_LOG_STRIPPED_DARK_OAK_LOG_STRIPPED, input = Material.DARK_OAK_LOG, output = Material.STRIPPED_DARK_OAK_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MANGROVE_LOG_STRIPPED_MANGROVE_LOG_STRIPPED, input = Material.MANGROVE_LOG, output = Material.STRIPPED_MANGROVE_LOG, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_OAK_WOOD_STRIPPED_OAK_WOOD_STRIPPED, input = Material.OAK_WOOD, output = Material.STRIPPED_OAK_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_SPRUCE_WOOD_STRIPPED_SPRUCE_WOOD_STRIPPED, input = Material.SPRUCE_WOOD, output = Material.STRIPPED_SPRUCE_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_BIRCH_WOOD_STRIPPED_BIRCH_WOOD_STRIPPED, input = Material.BIRCH_WOOD, output = Material.STRIPPED_BIRCH_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_JUNGLE_WOOD_STRIPPED_JUNGLE_WOOD_STRIPPED, input = Material.JUNGLE_WOOD, output = Material.STRIPPED_JUNGLE_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_ACACIA_WOOD_STRIPPED_ACACIA_WOOD_STRIPPED, input = Material.ACACIA_WOOD, output = Material.STRIPPED_ACACIA_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_CHERRY_WOOD_STRIPPED_CHERRY_WOOD_STRIPPED, input = Material.CHERRY_WOOD, output = Material.STRIPPED_CHERRY_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_PALE_OAK_WOOD_STRIPPED_PALE_OAK_WOOD_STRIPPED, input = Material.PALE_OAK_WOOD, output = Material.STRIPPED_PALE_OAK_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_DARK_OAK_WOOD_STRIPPED_DARK_OAK_WOOD_STRIPPED, input = Material.DARK_OAK_WOOD, output = Material.STRIPPED_DARK_OAK_WOOD, category = CenterType.STRIPPED),
			MasonRecipe(key = MultiblockRecipeKeys.STONECUTTING_MANGROVE_WOOD_STRIPPED_MANGROVE_WOOD_STRIPPED, input = Material.MANGROVE_WOOD, output = Material.STRIPPED_MANGROVE_WOOD, category = CenterType.STRIPPED),
		)

		recipeMap.forEach { it.boostrap(this) }
	}

	fun registerChemicalProcessorRecipes() {
		register(MultiblockRecipeKeys.TEST_CHEMICAL_PROCESSOR, ChemicalProcessorRecipe(
			key = MultiblockRecipeKeys.TEST_CHEMICAL_PROCESSOR,
			itemRequirement = MaterialRequirement(Material.IRON_INGOT),
			fluidRequirementOne = FluidRecipeRequirement("primaryin", FluidTypeKeys.OXYGEN, 10.0),
			fluidRequirementTwo = FluidRecipeRequirement("secondaryin", FluidTypeKeys.METHANE, 10.0),
			gridEnergyRequirement = GridEnergyRequirement(300.0, 1.0, Duration.ofSeconds(2)),
			fluidResultOne = FluidResult("primaryout", FluidStack(FluidTypeKeys.WATER, 10.0)),
			fluidResultTwo = FluidResult("secondaryout", FluidStack(FluidTypeKeys.CARBON_DIOXIDE, 10.0)),
			fluidResultPollutionResult = FluidResult("pollution", FluidStack(FluidTypeKeys.CARBON_DIOXIDE, 1.0)),
			itemResult = ResultHolder.of(ItemResult.simpleResult(CustomItemKeys.CIRCUITRY.getValue().constructItemStack())),
			resultSleepTicks = 2
		))
		register(MultiblockRecipeKeys.SABATIER_METHANE, ChemicalProcessorRecipe(
			key = MultiblockRecipeKeys.SABATIER_METHANE,
			itemRequirement = MaterialRequirement(Material.IRON_INGOT),
			fluidRequirementOne = FluidRecipeRequirement("primaryin", FluidTypeKeys.CARBON_DIOXIDE, 1.0),
			fluidRequirementTwo = FluidRecipeRequirement("secondaryin", FluidTypeKeys.HYDROGEN, 4.0),
			gridEnergyRequirement = GridEnergyRequirement(300.0, 1.0, Duration.ofSeconds(2)),

			fluidResultOne = FluidResult("primaryout", FluidStack(FluidTypeKeys.METHANE, 1.0)),
			fluidResultTwo = FluidResult("secondaryout", FluidStack(FluidTypeKeys.WATER, 1.0)),

			fluidResultPollutionResult = null,

			itemResult = ResultHolder.of(ItemResult.simpleResult(Material.IRON_INGOT)),
			resultSleepTicks = 2
		))
	}

	fun <E: RecipeEnviornment> getRecipesFor(entity: RecipeProcessingMultiblockEntity<E>): Collection<MultiblockRecipe<E>> {
		@Suppress("UNCHECKED_CAST")
		return byMultiblock[entity::class] as Collection<MultiblockRecipe<E>>
	}
}
