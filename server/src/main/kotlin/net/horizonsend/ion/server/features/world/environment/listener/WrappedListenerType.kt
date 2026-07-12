package net.horizonsend.ion.server.features.world.environment.listener

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import kotlin.reflect.KClass

class WrappedListenerType<T : Event>(override val key: IonRegistryKey<WrappedListenerType<*>, out WrappedListenerType<T>>, val eventClass: KClass<T>, val eventHandlerList: HandlerList, val priority: EventPriority, val ignoreCancelled: Boolean) : Keyed<WrappedListenerType<*>> {
	fun createInstance(handler: (T) -> Unit): WrappedListener<T> {
		return WrappedListener.SimpleWrappedListener(key, handler)
	}

	fun register(listener: WrappedListener<T>) {
		IonServer.server.pluginManager.registerEvent(
			eventClass.java,
			listener,
			priority,
			{ _, event ->
				if (!eventClass.isInstance(event)) return@registerEvent

				@Suppress("UNCHECKED_CAST")
				listener.recieveEvent(event as T)
			},
			IonServer,
			ignoreCancelled
		)

		listener.isRegistered = true
	}

	fun deRegister(listener: WrappedListener<T>) {
		eventHandlerList.unregister(listener)

		listener.isRegistered = false
	}
}
