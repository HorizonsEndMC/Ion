package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import java.util.EnumSet

val vaultEconomy = try {
	Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
} catch (exception: NoClassDefFoundError) {
	null
}

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }

/** Used for catching when a function that is not designed to be used async is being used async. */
fun mainThreadCheck() {
	if (!Bukkit.isPrimaryThread()) IonServer.Ion.slF4JLogger.warn("This function may be unsafe to use asynchronously.", Throwable())
}
