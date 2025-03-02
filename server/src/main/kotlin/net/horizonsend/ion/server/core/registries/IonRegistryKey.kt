package net.horizonsend.ion.server.core.registries

class IonRegistryKey<T : Any>(val registry: Registry<T>, val key: String) {
	override fun toString(): String {
		return "RegistryKey[${registry.id}:$key]"
	}

	fun getValue(): T {
		return registry[this]
	}
}
