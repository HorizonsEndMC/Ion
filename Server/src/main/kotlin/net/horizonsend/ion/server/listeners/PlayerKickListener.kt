package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

class PlayerKickListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
	fun onPlayerKickEvent(event: PlayerKickEvent) {
		// Really dumb solution for players being kicked due to "out of order chat messages"
		if (event.reason().toString().lowercase().contains("out-of-order")) {
			event.player.sendFeedbackMessage(
				FeedbackType.SERVER_ERROR,
				"The server attempted to kick you for out-of-order chat messages. You may need to retry any recent commands."
			)
			event.isCancelled = true
		}
	}
}