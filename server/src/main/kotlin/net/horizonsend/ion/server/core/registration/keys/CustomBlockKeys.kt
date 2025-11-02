package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.extractor.AdvancedItemExtractorBlock
import net.horizonsend.ion.server.features.custom.blocks.filter.ItemFilterBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.MultiblockWorkbench
import net.horizonsend.ion.server.features.custom.blocks.pipe.FluidPipeBlock
import net.horizonsend.ion.server.features.custom.blocks.pipe.FluidPipeJunctionBlock
import net.horizonsend.ion.server.features.custom.blocks.pipe.ReinforcedFluidPipeBlock
import net.horizonsend.ion.server.features.custom.blocks.pipe.ReinforcedFluidPipeJunctionBlock
import net.horizonsend.ion.server.features.custom.blocks.pipe.TemperatureGaugeBlock

object CustomBlockKeys : KeyRegistry<CustomBlock>(RegistryKeys.CUSTOM_BLOCKS, CustomBlock::class) {
	val ALUMINUM_ORE = registerKey("ALUMINUM_ORE")
	val ALUMINUM_BLOCK = registerKey("ALUMINUM_BLOCK")
	val RAW_ALUMINUM_BLOCK = registerKey("ARAW_ALUMINUM_BLOCK")

	val CHETHERITE_ORE = registerKey("CHETHERITE_ORE")
	val CHETHERITE_BLOCK = registerKey("CHETHERITE_BLOCK")

	val TITANIUM_ORE = registerKey("TITANIUM_ORE")
	val TITANIUM_BLOCK = registerKey("TITANIUM_BLOCK")
	val RAW_TITANIUM_BLOCK = registerKey("RAW_TITANIUM_BLOCK")

	val URANIUM_ORE = registerKey("URANIUM_ORE")
	val URANIUM_BLOCK = registerKey("URANIUM_BLOCK")
	val RAW_URANIUM_BLOCK = registerKey("RAW_URANIUM_BLOCK")

	val ENRICHED_URANIUM_BLOCK = registerKey("ENRICHED_URANIUM_BLOCK")
	val NETHERITE_CASING = registerKey("NETHERITE_CASING")
	val STEEL_BLOCK = registerKey("STEEL_BLOCK")
	val SUPERCONDUCTOR_BLOCK = registerKey("SUPERCONDUCTOR_BLOCK")

	val BATTLECRUISER_REACTOR_CORE = registerKey("BATTLECRUISER_REACTOR_CORE")
	val BARGE_REACTOR_CORE = registerKey("BARGE_REACTOR_CORE")
	val CRUISER_REACTOR_CORE = registerKey("CRUISER_REACTOR_CORE")

	val MULTIBLOCK_WORKBENCH = registerTypedKey<MultiblockWorkbench>("MULTIBLOCK_WORKBENCH")
	val ADVANCED_ITEM_EXTRACTOR = registerTypedKey<AdvancedItemExtractorBlock>("ADVANCED_ITEM_EXTRACTOR")
	val ITEM_FILTER = registerTypedKey<ItemFilterBlock>("ITEM_FILTER")

	val FLUID_PORT = registerKey("FLUID_PORT")
	val FLUID_VALVE = registerKey("FLUID_VALVE")
	val FLUID_PIPE = registerTypedKey<FluidPipeBlock>("FLUID_PIPE")
	val FLUID_PIPE_JUNCTION = registerTypedKey<FluidPipeJunctionBlock>("FLUID_PIPE_JUNCTION")
	val REINFORCED_FLUID_PIPE = registerTypedKey<ReinforcedFluidPipeBlock>("REINFORCED_FLUID_PIPE")
	val REINFORCED_FLUID_PIPE_JUNCTION = registerTypedKey<ReinforcedFluidPipeJunctionBlock>("REINFORCED_FLUID_PIPE_JUNCTION")
	val TEMPERATURE_GAUGE = registerTypedKey<TemperatureGaugeBlock>("TEMPERATURE_GAUGE")

	val COPPER_COIL = registerKey("COPPER_COIL")
	val ROTATION_SHAFT = registerKey("ROTATION_SHAFT")
	val REDSTONE_CONTROL_PORT = registerKey("REDSTONE_CONTROL_PORT")

	val GRID_ENERGY_PORT = registerKey("GRID_ENERGY_PORT")
}
