package net.horizonsend.ion.proxy.utils

import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.utils.Mutes.banCache
import net.horizonsend.ion.common.utils.Mutes.muteCache

fun Player.isMuted(): Boolean = muteCache[uniqueId]
fun Player.isBanned(): Boolean = banCache[uniqueId]

