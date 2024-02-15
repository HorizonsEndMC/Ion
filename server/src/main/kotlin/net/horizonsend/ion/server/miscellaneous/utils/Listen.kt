package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.IonServer
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

inline fun <reified T : Event> listen(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline block: (T) -> Unit
): Unit = listen<T>(priority, ignoreCancelled) { _, event -> block(event) }

inline fun <reified T : Event> listen(
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	noinline block: (Listener, T) -> Unit
) {
	IonServer.server.pluginManager.registerEvent(
		T::class.java,
		object : Listener {},
		priority,
		{ listener, event -> block(listener, event as? T ?: return@registerEvent) },
		IonServer,
		ignoreCancelled
	)
}
