package net.horizonsend.ion.proxy.features.messaging

import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.Server
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.types.CommonPlayerDataContainer
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import java.util.concurrent.TimeUnit

object PlayerTracking : IonProxyComponent() {
	override fun onEnable() {
		PLUGIN.proxy.scheduler.repeat(1000L, 1000L, TimeUnit.MILLISECONDS) { broadcastPlayers() }
	}

	private fun broadcastPlayers() = broadcastPlayersAction(CommonPlayerDataContainer(Server.PROXY, getPlayers()))

	private fun getPlayers(): List<CommonPlayer> = PLUGIN.proxy.players

	val broadcastPlayersAction = RedisAction.noOpAction<CommonPlayerDataContainer>("player-communication")

	init {
		RedisActions.register(broadcastPlayersAction)
	}
}
