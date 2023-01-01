package net.starlegacy.feature.nations.utils

import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.OfflinePlayer
import java.util.UUID

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
