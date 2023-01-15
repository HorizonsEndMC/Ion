package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val MULTIBLOCK = key("multiblock")
	val ORE_CHECK = key("oreCheck")
	val POWER = key("power")
	val ASTEROIDS_VERSION = key("asteroidsCheck")
	val ASTEROIDS = key("asteroids")
	val ASTEROIDS_ORES = key("asteroidsOres")
	val ASTEROIDS_WRECKS = key("asteroidsOres")

	fun key(key: String) = NamespacedKey(Ion, key)
}
