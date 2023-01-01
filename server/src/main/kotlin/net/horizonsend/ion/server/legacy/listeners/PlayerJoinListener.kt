package net.horizonsend.ion.server.legacy.listeners

import net.horizonsend.ion.server.legacy.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
	@Suppress("Unused")
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerJoin(event: PlayerJoinEvent) {
		if (event.player.hasProtection()) {
			event.player.sendFeedbackMessage(
				FeedbackType.INFORMATION,
				"You seem to be new here, it is generally recommended to use the Wiki when possible " +
					"(<white><u><click:open_url:'https://wiki.horizonsend.net'>wiki.horizonsend.net</click></u></white>). Feel " +
					"free to ask questions in chat if / when needed!"
			)
		}
	}
}
