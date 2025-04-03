package net.horizonsend.ion.server.features.multiblock.crafting

import io.papermc.paper.util.Tick
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.MultiblockRecipeKeys
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.GasCanisterRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement.MaterialRequirement
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
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.CenterType.PLANKS
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.CenterType.STRIPPED
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.STRIPPED_LOG_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STRIPPED_WOOD_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.UNSTRIPPED_LOG_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.UNSTRIPPED_WOOD_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.sound.Sound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory.StonecuttingRecipe
import java.time.Duration
import kotlin.reflect.KClass

class MultiblockRecipeRegistry : Registry<MultiblockRecipe<*>>(RegistryKeys.MULTIBLOCK_RECIPE) {
	override fun getKeySet(): KeyRegistry<MultiblockRecipe<*>> = MultiblockRecipeKeys

	private val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, MultiblockRecipe<*>>()

	override fun registerAdditional(key: IonRegistryKey<MultiblockRecipe<*>, out MultiblockRecipe<*>>, value: MultiblockRecipe<*>) {
		byMultiblock[value.entityType].add(value)
		registerAutoMasonRecipes()
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
	}

	private fun registerGasFurnaceRecipes() {
		register(MultiblockRecipeKeys.STEEL_PRODUCTION, FurnaceMultiblockRecipe(
			key = MultiblockRecipeKeys.STEEL_PRODUCTION,
			clazz = GasFurnaceMultiblock.GasFurnaceMultiblockEntity::class,
			smeltingItem = ItemRequirement.MaterialRequirement(Material.IRON_INGOT),
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

	private fun registerAutoMasonRecipes() {
		val recipeTable = CenterType.getRecipeTable()

		for (recipeHolder: RecipeHolder<*> in MinecraftServer.getServer().recipeManager.recipes.byType[RecipeType.STONECUTTING]) {
			val recipe = recipeHolder.toBukkitRecipe() as StonecuttingRecipe
			val input: ItemRequirement = when (val choice = recipe.inputChoice) {
				is MaterialChoice -> ItemRequirement.any(*choice.choices.map { MaterialRequirement(it) }.toTypedArray())
				is ExactChoice -> ItemRequirement.any(*choice.choices.map { ItemRequirement.ItemStackRequirement(it) }.toTypedArray())
				else -> ItemRequirement.ignore()
			}

			val result = recipe.result

			val inputMaterial = input.asItemStack()?.type ?: continue
			val centerType = CenterType[result.type]

			if (centerType == null) {
				log.warn("recipe ${recipe.key} doesn't have a registered center type for $inputMaterial to ${result.type}")
				continue
			}

			val tableResult = recipeTable.get(inputMaterial, centerType)

			if (tableResult == null) {
				log.warn("recipe ${recipe.key} doesn't have a registered result type for $inputMaterial $centerType ${result.type}")
				continue
			}

			registerAutoMasonRecipe(input, centerType, result)
		}

		UNSTRIPPED_LOG_TYPES.forEach { registerAutoMasonRecipe(MaterialRequirement(it), PLANKS, ItemStack(Material.valueOf(it.name.removeSuffix("_LOG") + "_PLANKS"), 4)) }
		UNSTRIPPED_WOOD_TYPES.forEach { registerAutoMasonRecipe(MaterialRequirement(it), PLANKS, ItemStack(Material.valueOf(it.name.removeSuffix("_WOOD") + "_PLANKS"), 4)) }
		STRIPPED_LOG_TYPES.forEach { registerAutoMasonRecipe(MaterialRequirement(it), PLANKS, ItemStack(Material.valueOf(it.name.removePrefix("STRIPPED_").removeSuffix("_LOG") + "_PLANKS"), 4)) }
		STRIPPED_WOOD_TYPES.forEach { registerAutoMasonRecipe(MaterialRequirement(it), PLANKS, ItemStack(Material.valueOf(it.name.removePrefix("STRIPPED_").removeSuffix("_WOOD") + "_PLANKS"), 4)) }

		UNSTRIPPED_LOG_TYPES.forEach { registerAutoMasonRecipe(MaterialRequirement(it), STRIPPED, ItemStack(Material.valueOf("STRIPPED_" + it.name))) }
		UNSTRIPPED_WOOD_TYPES.forEach { registerAutoMasonRecipe(MaterialRequirement(it), STRIPPED, ItemStack(Material.valueOf("STRIPPED_" + it.name))) }
	}

	private fun registerAutoMasonRecipe(input: ItemRequirement, category: CenterType, result: ItemStack) {
		register(AutoMasonRecipe(
			key = "STONECUTTING_${input}_$category",
			inputItem = input,
			centerCheck = category::matches,
			power = PowerRequirement(10),
			result = ResultHolder.of(ItemResult.simpleResult(result))
		))
	}

	fun <E: RecipeEnviornment, R: MultiblockRecipe<E>> register(recipe: R): R {
		recipes.add(recipe)
		byMultiblock[recipe.entityType].add(recipe)

		return recipe
	}

	fun getRecipes() = recipes

	fun <E: RecipeEnviornment> getRecipesFor(entity: RecipeProcessingMultiblockEntity<E>): Collection<MultiblockRecipe<E>> {
		@Suppress("UNCHECKED_CAST")
		return byMultiblock[entity::class] as Collection<MultiblockRecipe<E>>
	}
}
