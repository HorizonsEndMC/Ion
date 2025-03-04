package net.horizonsend.ion.server.core.registries

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

abstract class Registry<T : Any>(val id: String) {
	private val byRawString = Object2ObjectOpenHashMap<String, T>()

	abstract fun boostrap()

	protected open fun register(key: IonRegistryKey<T>, value: T) {
		byRawString[key.key] = value
		registerAdditional(key, value)
	}

	protected open fun registerAdditional(key: IonRegistryKey<T>, value: T) {}

	operator fun get(key: IonRegistryKey<T>): T {
		return byRawString[key.key] ?: error("Unregistered value $key!")
	}

	fun createKey(key: String): IonRegistryKey<T> = IonRegistryKey(this, key)

	fun getAll() = byRawString.values
}
