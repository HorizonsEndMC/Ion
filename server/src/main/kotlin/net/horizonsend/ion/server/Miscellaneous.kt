package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.Bukkit
import java.util.EnumSet

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }

/** Used for catching when a function that is not designed to be used async is being used async. */
fun mainThreadCheck() {
	if (!Bukkit.isPrimaryThread()) Ion.slF4JLogger.warn("This function may be unsafe to use asynchronously.", Throwable())
}
