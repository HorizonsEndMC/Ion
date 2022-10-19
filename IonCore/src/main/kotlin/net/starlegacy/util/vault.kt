package net.starlegacy.util

import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/** Registered vault economy service */
val VAULT_ECO: Economy = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)!!.provider

/** Registered vault permissions service */
val vaultChat: Chat = Bukkit.getServer().servicesManager.getRegistration(Chat::class.java)!!.provider

fun OfflinePlayer.getMoneyBalance(): Double = VAULT_ECO.getBalance(this)

fun OfflinePlayer.hasEnoughMoney(amount: Number): Boolean = VAULT_ECO.has(this, amount.toDouble())

fun OfflinePlayer.depositMoney(amount: Number) = VAULT_ECO.depositPlayer(this, amount.toDouble())

fun OfflinePlayer.withdrawMoney(amount: Number) = VAULT_ECO.withdrawPlayer(this, amount.toDouble())
