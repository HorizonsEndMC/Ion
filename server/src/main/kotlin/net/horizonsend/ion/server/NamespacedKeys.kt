package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val ORE_CHECK = key("oreCheck")
	val MULTIBLOCK = key("multiblock")
	val POWER = key("power")

	fun key(name: String) = NamespacedKey(Ion, name)
}
