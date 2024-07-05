package net.horizonsend.ion.proxy.utils

import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.slPlayerId

val Player.slPlayerId get() = uniqueId.slPlayerId
