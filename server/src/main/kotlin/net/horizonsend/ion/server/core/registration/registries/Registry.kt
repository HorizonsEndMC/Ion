package net.horizonsend.ion.server.core.registration.registries

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.IonResourceKey
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import kotlin.reflect.KClass

abstract class Registry<T : Any>(val id: IonResourceKey<Registry<T>>) {
	abstract fun getKeySet(): KeyRegistry<T>
	private val byRawString = Object2ObjectOpenHashMap<String, T>()

	abstract fun boostrap()

	protected open fun register(key: IonRegistryKey<T, out T>, value: T) {
		byRawString[key.key] = value
		registerAdditional(key, value)
	}

	protected open fun registerAdditional(key: IonRegistryKey<T, out T>, value: T) {}

	operator fun get(key: IonRegistryKey<T, out T>): T {
		return byRawString[key.key] ?: error("Unbound registy value $key!")
	}

	fun isBound(key: IonRegistryKey<T, out T>): Boolean {
		return byRawString.containsKey(key.key)
	}

	fun <Z : T> createKey(key: String, clazz: KClass<Z>): IonRegistryKey<T, Z> = IonRegistryKey(this, clazz, key)

	fun getAll() = byRawString.values
}
