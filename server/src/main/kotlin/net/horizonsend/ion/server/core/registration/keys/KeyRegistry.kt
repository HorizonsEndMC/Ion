package net.horizonsend.ion.server.core.registration.keys

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.core.registration.IonBindableResourceKey
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.registries.Registry
import org.bukkit.NamespacedKey
import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

abstract class KeyRegistry<T : Any>(private val registryId: IonBindableResourceKey<Registry<T>>, private val type: KClass<T>) {
	@Suppress("UNCHECKED_CAST")
	val registry: Registry<T> by lazy { IonRegistries[registryId] as Registry<T> }

	protected val keys = Object2ObjectOpenHashMap<String, IonRegistryKey<T, out T>>()
	protected val namespaced = Object2ObjectOpenHashMap<NamespacedKey, IonRegistryKey<T, out T>>()
	protected val allKeys = ObjectOpenHashSet<IonRegistryKey<T, out T>>()


	protected inline fun <reified Z : T> registerTypedKey(key: String): IonRegistryKey<T, Z> {
		val registryKey = registry.createKey(key, Z::class)
		keys[key] = registryKey
		namespaced[registryKey.ionNamespacedKey] = registryKey
		allKeys.add(registryKey)
		return registryKey
	}

	protected fun registerKey(key: String): IonRegistryKey<T, T> {
		val registryKey = registry.createKey(key, type)
		keys[key] = registryKey
		namespaced[registryKey.ionNamespacedKey] = registryKey
		allKeys.add(registryKey)
		return registryKey
	}

	operator fun get(string: String): IonRegistryKey<T, out T>? = keys[string]
	operator fun get(namespacedKey: NamespacedKey): IonRegistryKey<T, out T>? = namespaced[namespacedKey]

	fun getOrTrow(string: String): IonRegistryKey<T, out T> = keys[string] ?: throw IllegalArgumentException("Key $string not found for registry ${registryId.key}")
	fun getOrDefault(string: String, default: IonRegistryKey<T, out T>): IonRegistryKey<T, out T> = keys[string] ?: default

	fun allStrings() = keys.keys
	fun allkeys() = allKeys

	val serializer: IonRegistryKey.Serializer<T> = IonRegistryKey.Serializer(this)
	val listSerializer: ListPersistentDataType<String, IonRegistryKey<T, out T>> = PersistentDataType.LIST.listTypeFrom(serializer)
}
