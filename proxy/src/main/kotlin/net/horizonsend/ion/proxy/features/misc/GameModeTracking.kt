package net.horizonsend.ion.proxy.features.misc

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.scheduler.ScheduledTask
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.common.utils.miscellaneous.PlayerGameModeHolder
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import java.time.Duration
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object GameModeTracking : IonProxyComponent() {
	private lateinit var retrievalTask: ScheduledTask

	override fun onEnable() {
		retrievalTask = PLUGIN.server.scheduler.buildTask(PLUGIN, ::retrieveData)
			.repeat(Duration.ofSeconds(1))
			.schedule()
	}

	override fun onDisable() {
		retrievalTask.cancel()
	}

	private fun retrieveData() {
		redis {
			for (server in PLUGIN.server.allServers) {
				val name = server.serverInfo.name
				val key = "gamemode_$name"

				if (!exists(key)) return@redis 0

				val rawData = get(key) ?: continue
				val serverData = Configuration.parse<PlayerGameModeHolder>(rawData)

				serverMap[name] = serverData.players.associate { it.uuid to it.gameMode }
			}
		}
	}

	private val serverMap = mutableMapOf<String, Map<UUID, Int>>()

	val Player.gameMode: Int get() = readGameMode(this)

	private fun readGameMode(player: Player): Int {
		val server = player.currentServer.getOrNull()?.server?.serverInfo?.name ?: return -1

		return serverMap[server]?.get(player.uniqueId) ?: -1
	}
}
