package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")

	@Deprecated("") val MULTIBLOCK = key("multiblock")
	@Deprecated("") val ORE_CHECK = key("oreCheck")
	@Deprecated("") val POWER = key("power")

	fun key(key: String) = NamespacedKey(Ion, key)
}
