package net.horizonsend.ion.proxy.utils

import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.slPlayerId

object VelocityDBUtils {
	val Player.slPlayerId get() = uniqueId.slPlayerId
}
