package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.NamespacedKey

object NamespacedKeys {
	val ORE_CHECK = key("oreCheck")
	val MULTIBLOCK = key("multiblock")
	val POWER = key("power")
	val ASTEROIDS_CHECK = key("asteroidsCheck")
	val ASTEROIDS = key("asteroids")
	val ASTEROIDS_ORES = key("asteroidsOres")

	fun key(name: String) = NamespacedKey(Ion, name)
}
