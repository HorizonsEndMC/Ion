package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

class PlayerKickListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
	fun onPlayerKickEvent(event: PlayerKickEvent) {
		// Really dumb solution for players being kicked due to "out of order chat messages"
		if (event.reason() == Component.translatable("multiplayer.disconnect.out_of_order_chat")) {
			event.player.sendFeedbackMessage(
				FeedbackType.SERVER_ERROR,
				"Your last message / command was invalid, your system time may be out of sync. Please check that your time is synced at <u><click:open_url:'https://time.is'>time.is</click></u>, and then re-sync it if it is not."
			)
			event.isCancelled = true
		}
	}
}