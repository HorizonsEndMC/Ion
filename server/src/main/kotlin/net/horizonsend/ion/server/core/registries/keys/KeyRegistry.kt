package net.horizonsend.ion.server.core.registries.keys

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.Registry

abstract class KeyRegistry<T : Any>(val registry: Registry<T>) {
	private val keys = Object2ObjectOpenHashMap<String, IonRegistryKey<T>>()
	private val allKeys = ObjectOpenHashSet<IonRegistryKey<T>>()

	protected fun registerKey(key: String): IonRegistryKey<T> {
		val registryKey = registry.createKey(key)
		keys[key] = registryKey
		allKeys.add(registryKey)
		return registryKey
	}

	operator fun get(string: String) = keys[string]

	fun allStrings() = keys.keys
	fun allkeys() = allKeys
}
