package net.horizonsend.ion.server.core.registration

interface Keyed <T : Keyed<T>> {
	val key: IonRegistryKey<T, out T>
}
