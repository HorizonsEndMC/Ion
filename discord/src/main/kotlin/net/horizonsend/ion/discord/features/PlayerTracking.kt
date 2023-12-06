package net.horizonsend.ion.discord.features

import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.Server
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.types.CommonPlayerDataContainer

object PlayerTracking : IonComponent() {
	val trackedPlayers: Map<Server, MutableList<CommonPlayer>> = Server.values().associateWith { mutableListOf() }

	private val receivePlayersAction = object : RedisAction<CommonPlayerDataContainer>(
		"player-communication",
		object : TypeToken<CommonPlayerDataContainer>() {}.type,
		false
	) {
		override fun onReceive(data: CommonPlayerDataContainer) {
			val serverList = trackedPlayers[data.server]!!

			serverList.clear()
			serverList.addAll(data.players)
		}
	}

	fun getAllPlayers(): List<CommonPlayer> = trackedPlayers[Server.PROXY]!!
	fun getPlayers(server: Server): List<CommonPlayer> = trackedPlayers[server]!!

	init {
	    RedisActions.register(receivePlayersAction)
	}
}
