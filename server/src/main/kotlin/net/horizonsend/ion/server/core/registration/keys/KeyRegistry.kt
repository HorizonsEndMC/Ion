package net.horizonsend.ion.server.core.registration.keys

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.registries.Registry
import kotlin.reflect.KClass

abstract class KeyRegistry<T : Any>(val registry: Registry<T>, private val type: KClass<T>) {
	protected val keys = Object2ObjectOpenHashMap<String, IonRegistryKey<T, out T>>()
	protected val allKeys = ObjectOpenHashSet<IonRegistryKey<T, out T>>()

	protected inline fun <reified Z : T> registerTypedKey(key: String): IonRegistryKey<T, Z> {
		val registryKey = registry.createKey<Z>(key, Z::class)
		keys[key] = registryKey
		allKeys.add(registryKey)
		return registryKey
	}

	protected fun registerKey(key: String): IonRegistryKey<T, T> {
		val registryKey = registry.createKey(key, type)
		keys[key] = registryKey
		allKeys.add(registryKey)
		return registryKey
	}

	operator fun get(string: String): IonRegistryKey<T, out T>? = keys[string]

	fun allStrings() = keys.keys
	fun allkeys() = allKeys
}
