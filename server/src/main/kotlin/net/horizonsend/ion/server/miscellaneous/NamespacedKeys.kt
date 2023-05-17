package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
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

	// Ship factories
	val SHIP_FACTORY_DATA = key("ship_factory_data")
	val SHOW_BOUNDING_BOX = key("show_bounding_box")
	val RUNNING = key("show_bounding_box")
	val ROTATION = key("rotation")
	val BLUEPRINT = key("blueprint")

	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val EDEN_FIX = key("EdenFix")
	val BIOME_FIX = key("BiomeFix")

	val COMBAT_NPC = key("combatnpc")

	val MULTIBLOCK = key("multiblock")

	@Deprecated("") val ORE_CHECK = key("oreCheck")

	@Deprecated("") val POWER = key("power")

	val TUBE_BUTTONS = key("tube_buttons")
	val MATERIAL = key("material")

	fun key(key: String) = NamespacedKey(IonServer, key)
}
