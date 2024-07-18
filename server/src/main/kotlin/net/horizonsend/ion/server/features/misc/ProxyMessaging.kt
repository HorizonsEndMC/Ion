package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.common.utils.miscellaneous.PlayerGameModeHolder
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import kotlin.math.roundToInt

object ProxyMessaging : IonServerComponent() {
	private val serverName = IonServer.configuration.serverName?.lowercase() ?: "paper"

	override fun onEnable() {
		trackTPS()
		trackPlayerGameMode()
	}

	private fun trackTPS() {
		val key = "tps_$serverName"
		val tps = IonServer.server.tps[0].roundToInt().toString()

		Tasks.asyncRepeat(50L, 50L) {
			redis { set(key, tps) }
		}
	}

	private fun trackPlayerGameMode() {
		val key = "gamemode_$serverName"
		val data = PlayerGameModeHolder(
			Bukkit.getOnlinePlayers().map {
				PlayerGameModeHolder.PlayerGameModeEntry(it.uniqueId, it.gameMode.value)
			}
		)

		Tasks.asyncRepeat(50L, 50L) {
			redis { set(key, Configuration.write(data)) }
		}
	}
}
