package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val DATA_VERSION = key("data_version")

	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val EDEN_FIX = key("EdenFix")

	@Deprecated("") val MULTIBLOCK = key("multiblock")

	@Deprecated("") val ORE_CHECK = key("oreCheck")

	@Deprecated("") val POWER = key("power")

	fun key(key: String) = NamespacedKey(IonServer, key)
}
