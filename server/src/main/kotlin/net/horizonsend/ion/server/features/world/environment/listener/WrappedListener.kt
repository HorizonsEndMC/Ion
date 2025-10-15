package net.horizonsend.ion.server.features.world.environment.listener

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import org.bukkit.event.Event
import org.bukkit.event.Listener

abstract class WrappedListener<T : Event>(val type: IonRegistryKey<WrappedListenerType<*>, out WrappedListenerType<T>>) : Listener {
	var isRegistered: Boolean = false

	abstract fun recieveEvent(event: T)

	fun register() {
		return type.getValue().register(this)
	}

	fun deRegister() {
		return type.getValue().deRegister(this)
	}

	open class SimpleWrappedListener<T : Event>(type: IonRegistryKey<WrappedListenerType<*>, out WrappedListenerType<T>>, val handler: (T) -> Unit) : WrappedListener<T>(type) {
		override fun recieveEvent(event: T) {
			handler.invoke(event)
		}
	}
}
