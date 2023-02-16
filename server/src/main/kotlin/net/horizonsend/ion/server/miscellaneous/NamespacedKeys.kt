package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val EDEN_FIX = key("EdenFix")
	val STORED_CHUNK_BLOCKS = key("StoredChunkBlocks")
	val ASTEROIDS_VERSION = key("AsteroidsVersion")
	val WRECK_ENCOUNTER_DATA = key("WreckEncounterData")

	@Deprecated("") val MULTIBLOCK = key("multiblock")

	@Deprecated("") val ORE_CHECK = key("oreCheck")

	@Deprecated("") val POWER = key("power")

	fun key(key: String) = NamespacedKey(IonServer, key)
}
