package net.horizonsend.ion.server.legacy.utilities

import net.horizonsend.ion.server.vaultEconomy
import net.milkbowl.vault.economy.Economy
import java.util.EnumSet

inline fun vaultEconomy(execute: (Economy) -> Unit) {
	execute(vaultEconomy ?: return)
}

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }