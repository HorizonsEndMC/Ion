package net.horizonsend.ion.proxy.utils

import net.horizonsend.ion.common.utils.banCache
import net.horizonsend.ion.common.utils.muteCache
import net.md_5.bungee.api.connection.ProxiedPlayer

fun ProxiedPlayer.isMuted(): Boolean = muteCache[uniqueId]
fun ProxiedPlayer.isBanned(): Boolean = banCache[uniqueId]

