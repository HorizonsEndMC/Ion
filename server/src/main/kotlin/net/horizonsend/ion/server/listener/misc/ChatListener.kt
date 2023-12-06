package net.horizonsend.ion.server.listener.misc

import io.papermc.paper.event.player.AsyncChatEvent
import net.horizonsend.ion.common.utils.Mutes
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.chat.ChannelSelections
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.updateProtection
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

object ChatListener : SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onAsyncPlayerChatEventC(event: AsyncChatEvent) {
		if (event.isCancelled) return

		event.isCancelled = true

		if (Mutes.muteCache[event.player.uniqueId]) return

		event.player.updateProtection()

		val plainText = event.signedMessage().message()

		val channel = when {
			plainText.startsWith("!") -> ChatChannel.GLOBAL
			else -> ChannelSelections[event.player]
		}

		// For some reason the replace function returns a copy, no other adventure method does this
		event.message(event.message().replaceText { builder -> builder.matchLiteral("!").once().replacement("") })
		if (event.message().plainText().isEmpty()) return

		channel.onChat(event.player, event)
	}
}
