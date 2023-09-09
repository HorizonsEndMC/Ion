package net.horizonsend.ion.proxy.utils

import net.horizonsend.ion.common.database.slPlayerId
import net.md_5.bungee.api.connection.ProxiedPlayer

val ProxiedPlayer.slPlayerId get() = uniqueId.slPlayerId
