package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val MULTIBLOCK = key("multiblock")
	val ORE_CHECK = key("oreCheck")
	val POWER = key("power")

	fun key(key: String) = NamespacedKey(Ion, key)
}
