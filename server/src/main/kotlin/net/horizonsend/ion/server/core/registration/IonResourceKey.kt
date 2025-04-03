package net.horizonsend.ion.server.core.registration

abstract class IonResourceKey<T : Any>(val key: String) {
	abstract fun getValue(): T
}
