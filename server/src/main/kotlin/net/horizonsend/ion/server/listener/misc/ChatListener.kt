package net.horizonsend.ion.server.listener.misc

import io.papermc.paper.event.player.AsyncChatEvent
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.chat.ChannelSelections
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.horizonsend.ion.server.features.player.NewPlayerProtection.updateProtection
import net.horizonsend.ion.server.features.player.ServerMutesHook
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.concurrent.CompletableFuture

object ChatListener : SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onAsyncPlayerChatEventC(event: AsyncChatEvent) {
		if (event.isCancelled) return

		event.isCancelled = true

		val muted: CompletableFuture<Boolean> = ServerMutesHook.checkMute(event.player.uniqueId)

		muted.whenComplete { muted, exception ->
			if (exception != null) throw exception

			if (muted) return@whenComplete

			event.player.updateProtection()

			val plainText = event.signedMessage().message()

			val channel = when {
				plainText.startsWith("!") -> ChatChannel.GLOBAL
				else -> ChannelSelections[event.player]
			}

			// For some reason the replace function returns a copy, no other adventure method does this
			event.message(event.message().replaceText { builder ->
				builder
					.match("^!")
					.once()
					.replacement("")
			})

			if (event.message().plainText().isEmpty()) return@whenComplete

			channel.onChat(event.player, event)
		}
	}
}
