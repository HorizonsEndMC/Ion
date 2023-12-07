package net.horizonsend.ion.common.utils.redis.messaging

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.utils.discord.Channel
import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.kyori.adventure.text.Component
import java.util.UUID

abstract class Notifications : IonComponent() {
	// notify-online
	abstract val notifyOnlineAction: RedisAction<Component>
	// notify-player
	abstract val notifyPlayerAction: RedisAction<Pair<UUID, Component>>
	// notify-settlement
	abstract val notifySettlementAction: RedisAction<Pair<String, Component>>
	// notify-nation
	abstract val notifyNationAction: RedisAction<Pair<String, Component>>

	override fun onEnable() {
		RedisActions.register(notifyOnlineAction)
	}

	data class DiscordMessage(
		val channel: Channel,
		val message: Component
	)
}
