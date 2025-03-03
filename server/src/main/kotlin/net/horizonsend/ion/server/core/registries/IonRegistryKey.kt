package net.horizonsend.ion.server.core.registries

import net.horizonsend.ion.server.IonServer
import org.bukkit.NamespacedKey

class IonRegistryKey<T : Any>(val registry: Registry<T>, val key: String) {
	override fun toString(): String {
		return "RegistryKey[${registry.id}:$key]"
	}

	fun getValue(): T {
		return registry[this]
	}

	val ionNapespacedKey = NamespacedKey(IonServer, key)
}
