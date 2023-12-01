package net.horizonsend.ion.proxy.utils

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.utils.discord.Channel

object RedisActions : IonComponent() {
	val discord_action = { (channel, serializedEmbed): Pair<Channel, String> -> }.registerRedisAction("notify-discord", runSync = false)
}
