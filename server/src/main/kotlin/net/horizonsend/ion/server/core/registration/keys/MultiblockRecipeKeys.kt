package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe

object MultiblockRecipeKeys : KeyRegistry<MultiblockRecipe<*>>(IonRegistries.MULTIBLOCK_RECIPE, MultiblockRecipe::class) {
	val URANIUM_ENRICHMENT = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("URANIUM_ENRICHMENT")
	val URANIUM_CORE_COMPRESSION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("URANIUM_CORE_COMPRESSION")
	val STEEL_PRODUCTION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("STEEL_PRODUCTION")
	val REACTIVE_PLATING_PRESSING = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("REACTIVE_PLATING_PRESSING")
	val STEEL_PLATE_PRESSING = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("STEEL_PLATE_PRESSING")
	val FUEL_ROD_CORE_FABRICATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("FUEL_ROD_CORE_FABRICATION")
	val FABRICATED_ASSEMBLY_FABRICATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("FABRICATED_ASSEMBLY_FABRICATION")
	val REINFORCED_FRAME_FABRICATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("REINFORCED_FRAME_FABRICATION")
	val CIRCUIT_BOARD_FABRICATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("CIRCUIT_BOARD_FABRICATION")
	val LOADED_SHELL_LOADING = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("LOADED_SHELL_LOADING")
	val UNCHARGED_SHELL_CHARGING = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("UNCHARGED_SHELL_CHARGING")
	val ARSENAL_MISSILE_LOADING = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("ARSENAL_MISSILE_LOADING")

	val COPPER_BLOCK_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("COPPER_BLOCK_OXIDATION")
	val EXPOSED_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_COPPER_OXIDATION")
	val WEATHERED_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_COPPER_OXIDATION")
	val CHISELED_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("CHISELED_COPPER_OXIDATION")
	val EXPOSED_CHISELED_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_CHISELED_COPPER_OXIDATION")
	val WEATHERED_CHISELED_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_CHISELED_COPPER_OXIDATION")
	val COPPER_GRATE_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("COPPER_GRATE_OXIDATION")
	val EXPOSED_COPPER_GRATE_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_COPPER_GRATE_OXIDATION")
	val WEATHERED_COPPER_GRATE_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_COPPER_GRATE_OXIDATION")
	val CUT_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("CUT_COPPER_OXIDATION")
	val EXPOSED_CUT_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_CUT_COPPER_OXIDATION")
	val WEATHERED_CUT_COPPER_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_CUT_COPPER_OXIDATION")
	val CUT_COPPER_STAIRS_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("CUT_COPPER_STAIRS_OXIDATION")
	val EXPOSED_CUT_COPPER_STAIRS_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_CUT_COPPER_STAIRS_OXIDATION")
	val WEATHERED_CUT_COPPER_STAIRS_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_CUT_COPPER_STAIRS_OXIDATION")
	val CUT_COPPER_SLAB_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("CUT_COPPER_SLAB_OXIDATION")
	val EXPOSED_CUT_COPPER_SLAB_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_CUT_COPPER_SLAB_OXIDATION")
	val WEATHERED_CUT_COPPER_SLAB_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_CUT_COPPER_SLAB_OXIDATION")
	val COPPER_DOOR_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("COPPER_DOOR_OXIDATION")
	val EXPOSED_COPPER_DOOR_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_COPPER_DOOR_OXIDATION")
	val WEATHERED_COPPER_DOOR_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_COPPER_DOOR_OXIDATION")
	val COPPER_TRAPDOOR_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("COPPER_TRAPDOOR_OXIDATION")
	val EXPOSED_COPPER_TRAPDOOR_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_COPPER_TRAPDOOR_OXIDATION")
	val WEATHERED_COPPER_TRAPDOOR_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_COPPER_TRAPDOOR_OXIDATION")
	val COPPER_BULB_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("COPPER_BULB_OXIDATION")
	val EXPOSED_COPPER_BULB_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("EXPOSED_COPPER_BULB_OXIDATION")
	val WEATHERED_COPPER_BULB_OXIDATION = registerTypedKey<MultiblockRecipe<FurnaceEnviornment>>("WEATHERED_COPPER_BULB_OXIDATION")
}
