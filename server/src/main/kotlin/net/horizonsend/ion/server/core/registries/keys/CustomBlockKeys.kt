package net.horizonsend.ion.server.core.registries.keys

import net.horizonsend.ion.server.core.registries.IonRegistries
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock

object CustomBlockKeys : KeyRegistry<CustomBlock>(IonRegistries.CUSTOM_BLOCKS) {
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

	val MULTIBLOCK_WORKBENCH = registerKey("MULTIBLOCK_WORKBENCH")
	val ADVANCED_ITEM_EXTRACTOR = registerKey("ADVANCED_ITEM_EXTRACTOR")
	val ITEM_FILTER = registerKey("ITEM_FILTER")
}
