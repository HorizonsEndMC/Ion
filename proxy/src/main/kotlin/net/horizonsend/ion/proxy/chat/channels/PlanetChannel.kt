package net.horizonsend.ion.proxy.chat.channels

import com.google.gson.reflect.TypeToken
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import litebans.api.Database
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.proxy.chat.Channel
import net.horizonsend.ion.proxy.utils.isMuted
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

class PlanetChannel : Channel {
	override val name = "planet"
	override val prefix = ""
	override val displayName = "<blue>Planet"
	override val commands = listOf("planetchat", "pc")
	override val color = NamedTextColor.BLUE
	override val checkPermission = true

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		if (player.isMuted()) {
			return false
		}

		RedisActions.publish<Pair<UUID, String>>(
			"planet-chat",
			player.uniqueId to event.message,
			object : TypeToken<Pair<UUID, String>>() {}.type
		)

		return false
	}
}
