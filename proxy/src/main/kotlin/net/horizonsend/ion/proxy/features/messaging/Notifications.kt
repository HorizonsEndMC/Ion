package net.horizonsend.ion.proxy.features.messaging

import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.messaging.Notifications
import net.kyori.adventure.text.Component
import java.util.UUID

object Notifications : Notifications() {
	override val notifyOnlineAction: RedisAction<Component> = RedisAction.noOpAction("notify-online")
	override val notifyPlayerAction: RedisAction<Pair<UUID, Component>> = RedisAction.noOpAction("notify-player")
	override val notifySettlementAction: RedisAction<Pair<String, Component>> = RedisAction.noOpAction("notify-settlement")
	override val notifyNationAction: RedisAction<Pair<String, Component>> = RedisAction.noOpAction("notify-nation")
}
