package net.horizonsend.ion.proxy.utils

import com.velocitypowered.api.proxy.Player
import litebans.api.Database

fun Player.isMuted(): Boolean {
	return try {
		Database.get().isPlayerMuted(uniqueId, null)
	} catch (_: Throwable) {
		false
	} // no litebans :(
}
