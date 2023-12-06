package net.horizonsend.ion.proxy.features

import com.google.gson.reflect.TypeToken
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
		PLUGIN.proxy.scheduler.repeat(1000L, 1000L, TimeUnit.MILLISECONDS) {
			broadcastPlayersAction(CommonPlayerDataContainer(Server.DISCORD_BOT, getPlayers()))
		}
	}

	private fun getPlayers(): List<CommonPlayer> = PLUGIN.proxy.players

	val broadcastPlayersAction = object : RedisAction<CommonPlayerDataContainer>(
		"player-communication",
		object : TypeToken<CommonPlayerDataContainer>() {}.type,
		false
	) {
		// Do nothing
		override fun onReceive(data: CommonPlayerDataContainer) {}
	}

	init {
		RedisActions.register(broadcastPlayersAction)
	}
}
