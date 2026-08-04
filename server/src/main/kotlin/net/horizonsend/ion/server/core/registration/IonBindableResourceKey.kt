package net.horizonsend.ion.server.core.registration

abstract class IonBindableResourceKey<T : Any>(key: String) : IonResourceKey<T>(key) {
	abstract fun getValue(): T
}
