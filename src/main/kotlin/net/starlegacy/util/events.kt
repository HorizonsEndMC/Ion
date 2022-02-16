package net.starlegacy.util

import ninja.egg82.events.BukkitEventSubscriber
import ninja.egg82.events.BukkitEvents
import org.bukkit.event.Event
import org.bukkit.event.EventPriority

inline fun <reified T : Event> subscribe(priority: EventPriority = EventPriority.NORMAL): BukkitEventSubscriber<T> {
	return BukkitEvents.subscribe(T::class.java, priority).filtered { it is T }
}

fun <T : Event> BukkitEventSubscriber<T>.filtered(filter: (T) -> Boolean): BukkitEventSubscriber<T> {
	return this.filter { e: T -> filter(e) }
}