package net.horizonsend.ion.server.features.nations.utils

import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID
import java.util.function.Consumer

fun getPing(player: Player): Int {
	return player.minecraft.connection.latency()
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

fun toPlayersInRadius(origin: Location, radius: Double, consumer: Consumer<Player>) = origin.getNearbyPlayers(radius).forEach(consumer)

fun playSoundInRadius(origin: Location, radius: Double, sound: Sound) {
	toPlayersInRadius(origin, radius) { player ->
		player.playSound(sound)
	}
}
