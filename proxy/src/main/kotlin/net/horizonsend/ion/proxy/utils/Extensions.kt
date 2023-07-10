package net.horizonsend.ion.proxy.utils

import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.slPlayerId

object Extensions {
	val Player.slPlayerId get() = uniqueId.slPlayerId


}
