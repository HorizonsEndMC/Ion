package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

object NamespacedKeys {
	// Generic Coordinates
	val X = key("x")
	val Y = key("y")
	val Z = key("z")

	val BLOCK_KEY = key("block_key")

	val AXIS = key("axis")
	val ROTATION = key("rotation")

	val UP_DOWN = key("up_down")
	val LEFT_RIGHT = key("left_right")
	val FORWARD_BACKWARD = key("left_right")

	// Block Data PDC
	val BLOCK_STATE = key("block_state")
	val BLOCK_ENTITY = key("block_entity")

	val TUBE_BUTTONS = key("tube_buttons")
	val MATERIAL = key("material")

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
	val USER = key("user")
	val USER_NAME = key("user_name")

	@Deprecated("") val ORE_CHECK = key("oreCheck")

	val PROGRESS = key("progress")
	val LAST_PROGRESS_TICK = key("last_progress_tick")
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

	val POWER = key("power")
	val GAS = key("Gas")

	val SERIALIZATION_TYPE = key("serialization_type")
	val META_DATA = key("meta_data")

	val CHUNK_FILTER_DATA = key("chunk_filter_data")
	val FILTER_DATA = key("filter_data")
	val FILTER_TYPE = key("filter_type")
	val FILTER_ENTRY = key("filter_entry")
	val SORTING_METHOD = key("sorting_method")
	val FILTER_META = key("filter_meta")
	val WHITELIST = key("whitelist")

	val POWER_TRANSPORT = key("power_transport")
	val FLUID_TRANSPORT = key("gas_transport")
	val ITEM_TRANSPORT = key("item_transport")

	val STORED_MULTIBLOCK_ENTITIES = key("stored_multiblock_entities")
	val STORED_MULTIBLOCK_ENTITIES_OLD = key("stored_multiblock_entities_old")

	val STANDARD_EXTRACTORS = key("standard_extractors")
	val COMPLEX_EXTRACTORS = key("complex_extractors")

	val SORTING_ORDER = key("sorting_order")

	val NODE_TYPE = key("node_type")

	// Fluid storage
	val FLUID = key("fluid")
	val FLUID_AMOUNT = key("fluid_amount")

	val STORAGES = key("storages")
	val MAIN_STORAGE = key("main_storage")

	val TANK_1 = key("tank_1")
	val TANK_2 = key("tank_2")
	val TANK_3 = key("tank_3")

	val BLUEPRINT_NAME = key("blueprint_name")
	val BLUEPRINT_ID = key("blueprint_id")

	val FIRST_POINT = key("first_point")
	val SECOND_POINT = key("second_point")

	fun key(key: String) = NamespacedKey(IonServer, key)

	// Used for datapacks and resource packs
	const val HORIZONSEND_NAMESPACE = "horizonsend"

	fun packKey(key: String) = Key.key(HORIZONSEND_NAMESPACE, key)
}
