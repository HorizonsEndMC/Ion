package net.horizonsend.ion.server.legacy.utilities

import net.horizonsend.ion.server.vaultEconomy
import net.milkbowl.vault.economy.Economy

inline fun vaultEconomy(execute: (Economy) -> Unit) {
	execute(vaultEconomy ?: return)
}