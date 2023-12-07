package net.horizonsend.ion.server.features.misc.messaging

import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.types.CommonPlayerDataContainer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.PlayerWrapper.Companion.common
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

object PlayerTracking : IonServerComponent() {
	override fun onEnable() {
		Tasks.asyncRepeat(20L, 20L) { broadcastPlayers() }
	}

	private fun getPlayers(): List<CommonPlayer> = IonServer.server.onlinePlayers.map { it.common() }

	val broadcastPlayersAction = object : RedisAction<CommonPlayerDataContainer>(
		"player-communication",
		object : TypeToken<CommonPlayerDataContainer>() {}.type,
		false
	) {
		// Do nothing
		override fun onReceive(data: CommonPlayerDataContainer) {}
	}

	private fun broadcastPlayers() = broadcastPlayersAction(CommonPlayerDataContainer(CommonConfig.common.serverType, getPlayers()))

	init {
		RedisActions.register(broadcastPlayersAction)
	}
}

