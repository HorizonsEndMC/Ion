package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

object NamespacedKeys {
	// Generic Coordinates
	val X = key("x")
	val Y = key("y")
	val Z = key("z")

	val UP_DOWN = key("up_down")
	val LEFT_RIGHT = key("left_right")
	val FORWARD_BACKWARD = key("left_right")

	// Block Data PDC
	val BLOCK_STATE = key("block_state")
	val BLOCK_ENTITY = key("block_entity")

	// Encounters PDC
	val ENCOUNTER = key("encounter")

	// Encounter Chests PDC
	val SECONDARY_CHEST = key("secondary_chest")
	val SECONDARY_CHEST_MONEY = key("secondary_chest_money")

	// Spacegen block storage
	val STORED_CHUNK_BLOCKS = key("stored_chunk_blocks")
	val SECTIONS = key("palette")
	val PALETTE = key("palette")
	val BLOCKS = key("blocks")
	val SPACE_GEN_VERSION = key("space_gen_version")

	// Various encounter flags
	val INACTIVE = key("inactive")
	val LOCKED = key("locked")

	val AMMO = key("Ammo")
	val GAS = key("Gas")
	val CUSTOM_ITEM = key("CustomItem")
	val EDEN_FIX = key("EdenFix")
	val BIOME_FIX = key("BiomeFix")

	val COMBAT_NPC = key("combatnpc")

	val MULTIBLOCK = key("multiblock")
	val ADDITIONAL_MULTIBLOCK_DATA = key("additional_multiblock_data")
	val MULTIBLOCK_ENTITY_DATA = key("multiblock_entity_data")
	val MULTIBLOCK_SIGN_OFFSET = key("multiblock_sign_offset")

	val CUSTOM_ENTITY = key("custom_entity")

	val SPLITTER_DIRECTION = key("splitter_direction")
	val DRILL_USER = key("drill_user")

	@Deprecated("") val ORE_CHECK = key("oreCheck")

	@Deprecated("") val POWER = key("power")

	val TUBE_BUTTONS = key("tube_buttons")
	val MATERIAL = key("material")

	val PROGRESS = key("progress")
	val CUSTOM_ITEM_RESULT = key("custom_item_result")

	val ORE_POSITIONS = key("ore_positions")
	val ORE_INDEXES = key("ore_indexes")
	val ORE_DATA = key("ore_data")
	val ORE_REPLACED_INDEXES = key("ore_replaced_indexes")
	val ORE_REPLACED = key("ore_replaced")

	val PLAYER_DATA_VERSION = key("player_data_version")
	val DATA_VERSION = key("data_version")
	val BLOCKS_TRAVELED = key("blocks_traveled")
	val HYPERSPACE_BLOCKS_TRAVELED = key("hyperspace_blocks_traveled")

	val TOOL_MODIFICATIONS = key("tool_modifications")

	val FORBIDDEN_BLOCKS = key("forbidden_blocks")
	val CARGO_CRATE = key("cargo_crate")

	val STORED_MULTIBLOCK_ENTITIES = key("stored_multiblock_entities")
	val EXTRACTOR_DATA = key("extractor_data")

	val PROCESSING_PROGRESS = key("processing_progress")

	fun key(key: String) = NamespacedKey(IonServer, key)

	// Used for datapacks and resource packs
	const val HORIZONSEND_NAMESPACE = "horizonsend"

	fun packKey(key: String) = Key.key(HORIZONSEND_NAMESPACE, key)
}
