package net.horizonsend.ion.server.core.registries

import net.horizonsend.ion.server.IonServer
import org.bukkit.NamespacedKey
import kotlin.reflect.KClass

class IonRegistryKey<T : Any, Z : T>(val registry: Registry<T>, val clazz: KClass<out Z>, val key: String) {
	override fun toString(): String {
		return "RegistryKey[${registry.id}:$key]"
	}

	fun getValue(): Z {
		val stored =  registry[this]

		if (!clazz.isInstance(stored)) {
			error("The stored value at key $this is not of matching type ${clazz.simpleName}")
		}

		@Suppress("UNCHECKED_CAST")
		return stored as Z
	}

	val ionNapespacedKey = NamespacedKey(IonServer, key)
}
