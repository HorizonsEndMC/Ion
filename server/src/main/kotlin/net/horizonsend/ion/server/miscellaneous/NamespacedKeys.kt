package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val DATA_VERSION = key("data_version")

	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val EDEN_FIX = key("EdenFix")
	val STORED_CHUNK_BLOCKS = key("StoredChunkBlocks")
	val SPACE_GEN_VERSION = key("SpaceGenVersion")
	val WRECK_ENCOUNTER_DATA = key("WreckEncounterData")
	val WRECK_CHEST_LOCK = key("WreckChestLock")

	@Deprecated("") val MULTIBLOCK = key("multiblock")

	@Deprecated("") val ORE_CHECK = key("oreCheck")

	@Deprecated("") val POWER = key("power")

	fun key(key: String) = NamespacedKey(IonServer, key)
}
