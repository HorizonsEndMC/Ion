package net.horizonsend.ion.server.features.misc.messaging

import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.messaging.DiscordMessages

object ServerDiscordMessaging : DiscordMessages() {
	override val notifyAction: RedisAction<DiscordMessage> = RedisAction.noOpAction("notify-discord")
	override val notifyEmbedAction: RedisAction<DiscordEmbedMessage> = RedisAction.noOpAction("notify-discord-embed")
}
