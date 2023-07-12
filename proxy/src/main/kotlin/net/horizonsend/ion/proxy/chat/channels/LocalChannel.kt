package net.horizonsend.ion.proxy.chat.channels

import com.google.gson.reflect.TypeToken
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.proxy.chat.Channel
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

class LocalChannel : Channel {
	override val name = "local"
	override val prefix = ""
	override val displayName = "<yellow>Local"
	override val commands = listOf("global", "l")
	override val color = NamedTextColor.YELLOW
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		RedisActions.publish<Pair<UUID, String>>(
			"local-chat",
			player.uniqueId to event.message,
			object : TypeToken<Pair<UUID, String>>() {}.type
		)

		return false
	}
}
