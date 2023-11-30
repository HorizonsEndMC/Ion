package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.common.utils.Mutes
import net.horizonsend.ion.server.features.chat.ChannelSelections
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.updateProtection
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.vaultChat
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

object ChatListener : SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onAsyncPlayerChatEventA(event: AsyncPlayerChatEvent) {
		event.player.updateProtection()
		val prefix = vaultChat.getPlayerPrefix(event.player)
		val suffix = vaultChat.getPlayerSuffix(event.player)
		event.format = "$prefix%s$suffix ${SLTextStyle.DARK_GRAY}» ${SLTextStyle.RESET}%s".colorize()

		if (!event.message.startsWith("!")) {
			val channel = ChannelSelections[event.player]
			event.message = "${channel.messageColor}${event.message}"
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun onAsyncPlayerChatEventB(event: AsyncPlayerChatEvent) {
		event.player.updateProtection()
		event.format = "&8[&b${Levels[event.player]}&8]&7 ".colorize() + event.format
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onAsyncPlayerChatEventC(event: AsyncPlayerChatEvent) {
		if (event.isCancelled) return

		event.isCancelled = true

		event.player.updateProtection()

		val channel = when {
			event.message.startsWith("!") -> {
				if (Mutes.muteCache[event.player.uniqueId]) return
				ChatChannel.GLOBAL
			}
			else -> ChannelSelections[event.player]
		}

		event.message = event.message.removePrefix("!").trim()
		if (event.message.isBlank()) return

		channel.onChat(event.player, event)
	}
}
