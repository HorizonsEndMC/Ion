package net.horizonsend.ion.server.core.registries.keys

import net.horizonsend.ion.server.core.registries.IonRegistryKey

interface Keyed <T : Any> {
	val key: IonRegistryKey<T, out T>
}
