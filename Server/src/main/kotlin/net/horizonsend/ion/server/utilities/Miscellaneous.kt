package net.horizonsend.ion.server.utilities

import java.util.EnumSet
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

inline fun vaultEconomy(execute: (Economy) -> Unit) {
	execute(Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider ?: return)
}

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }