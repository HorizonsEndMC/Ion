package net.horizonsend.ion.proxy.utils

import com.google.gson.reflect.TypeToken
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.discord.Channel
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.actions.RedisResponseAction

object RedisActions : IonComponent() {
	val discord_action = { (channel, serializedEmbed): Pair<Channel, String> -> }.registerRedisAction("notify-discord", runSync = false)

	val getPlayersAction = object : RedisResponseAction<String, List<CommonPlayer>>(
		"get-players",
		object : TypeToken<String>() {}.type,
		false
	) {
		override fun createReply(data: String): List<CommonPlayer> {
			return listOf()
		}
	}.apply {
		RedisActions.register(this)
		log.info("Registered action $id")
	}
}
