package net.horizonsend.ion.server.core.registration.keys

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.IonResourceKey
import net.horizonsend.ion.server.core.registration.registries.Registry
import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

abstract class KeyRegistry<T : Any>(private val registryId: IonResourceKey<Registry<T>>, private val type: KClass<T>) {
	@Suppress("UNCHECKED_CAST")
	val registry: Registry<T> by lazy { IonRegistries[registryId] as Registry<T> }

	protected val keys = Object2ObjectOpenHashMap<String, IonRegistryKey<T, out T>>()
	protected val allKeys = ObjectOpenHashSet<IonRegistryKey<T, out T>>()

	protected inline fun <reified Z : T> registerTypedKey(key: String): IonRegistryKey<T, Z> {
		val registryKey = registry.createKey(key, Z::class)
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
	fun getOrTrow(string: String): IonRegistryKey<T, out T> = keys[string] ?: throw IllegalArgumentException("Key $string not found for registry ${registryId.key}")

	fun allStrings() = keys.keys
	fun allkeys() = allKeys

	val serializer: KeyRegistry<T>.PDCKeySerializer = PDCKeySerializer()
	val listSerializer: ListPersistentDataType<String, IonRegistryKey<T, out T>> = PersistentDataType.LIST.listTypeFrom(serializer)

	inner class PDCKeySerializer() : PersistentDataType<String, IonRegistryKey<T, out T>> {
		override fun getPrimitiveType(): Class<String> = String::class.java
		@Suppress("UNCHECKED_CAST")
		override fun getComplexType(): Class<IonRegistryKey<T, out T>> = IonRegistryKey::class.java as Class<IonRegistryKey<T, out T>>

		override fun toPrimitive(complex: IonRegistryKey<T, out T>, context: PersistentDataAdapterContext): String {
			return complex.key
		}

		override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): IonRegistryKey<T, out T> {
			return this@KeyRegistry[primitive] ?: throw IllegalArgumentException("$primitive key not found for key registry ${registryId.key}")
		}
	}
}
