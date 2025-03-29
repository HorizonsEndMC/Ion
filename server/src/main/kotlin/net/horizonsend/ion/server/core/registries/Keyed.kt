package net.horizonsend.ion.server.core.registries

interface Keyed <T : Any> {
	val key: IonRegistryKey<T, out T>
}
