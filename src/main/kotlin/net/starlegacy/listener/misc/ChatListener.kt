package net.starlegacy.listener.misc

import net.starlegacy.feature.chat.ChannelSelections
import net.starlegacy.feature.chat.ChatChannel
import net.starlegacy.feature.progression.Levels
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.colorize
import net.starlegacy.util.filtered
import net.starlegacy.util.subscribe
import net.starlegacy.util.vaultChat
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

object ChatListener : SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	override fun onRegister() {
		subscribe<AsyncPlayerChatEvent>(EventPriority.LOWEST).handler { event ->
			val prefix = vaultChat.getPlayerPrefix(event.player)
			val suffix = vaultChat.getPlayerSuffix(event.player)
			event.format = "$prefix%s$suffix ${SLTextStyle.DARK_GRAY}» ${SLTextStyle.RESET}%s".colorize()

			if (!event.message.startsWith("!")) {
				val channel = ChannelSelections[event.player]
				event.message = "${channel.messageColor}${event.message}"
			}
		}

		subscribe<AsyncPlayerChatEvent>(EventPriority.HIGH).handler { event ->
			event.format = "&8[&l${Levels[event.player]}&8]&7 ".colorize() + event.format
		}

		subscribe<AsyncPlayerChatEvent>(EventPriority.HIGHEST)
			.filtered { !it.isCancelled }
			.handler { event ->
				event.isCancelled = true

				val channel = when {
					event.message.startsWith("!") -> ChatChannel.GLOBAL
					else -> ChannelSelections[event.player]
				}
				event.message = event.message.removePrefix("!").trim()
				if (event.message.isBlank()) {
					return@handler
				}
				channel.onChat(event.player, event)
			}
	}
}
