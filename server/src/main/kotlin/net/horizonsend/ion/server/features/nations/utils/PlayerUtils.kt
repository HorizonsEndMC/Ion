package net.horizonsend.ion.server.features.nations.utils

import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

fun getPing(player: Player): Int {
	return player.minecraft.connection.player.latency
}

private fun isInvalid(offlinePlayer: OfflinePlayer) =
	offlinePlayer.uniqueId == null || !offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline

fun findPlayerIdByName(name: String): UUID? = Bukkit.createProfile(name).let {
	return if (!it.complete(false)) null else it.id
}

fun findOfflinePlayer(id: UUID?): OfflinePlayer? {
	try {
		if (id == null) return null

		val offlinePlayer = getOfflinePlayer(id)
		return if (isInvalid(offlinePlayer)) null else offlinePlayer
	} catch (e: Exception) {
		return null
	}
}

@Deprecated("Use profiles")
fun findOfflinePlayer(name: String?): OfflinePlayer? {
	return findOfflinePlayer(findPlayerIdByName(name ?: return null))
}
