package net.horizonsend.ion.proxy.utils

import litebans.api.Database
import net.md_5.bungee.api.connection.ProxiedPlayer

fun ProxiedPlayer.isMuted(): Boolean = runCatching { Database.get().isPlayerMuted(uniqueId, null) }.getOrDefault(false)
fun ProxiedPlayer.isBanned(): Boolean = runCatching { Database.get().isPlayerBanned(uniqueId, null) }.getOrDefault(false)

