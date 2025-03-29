package net.horizonsend.ion.server.core.registration

interface Keyed <T : Any> {
	val key: IonRegistryKey<T, out T>
}
