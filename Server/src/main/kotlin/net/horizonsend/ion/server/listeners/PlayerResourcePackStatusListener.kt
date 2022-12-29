package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class PlayerResourcePackStatusListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerResourcePackStatusEvent(event: PlayerResourcePackStatusEvent) {
		if (event.status != PlayerResourcePackStatusEvent.Status.ACCEPTED) return

		event.player.sendMessage(
			MiniMessage.miniMessage().deserialize(
				"<${FeedbackType.USER_ERROR.colour}>Please consider downloading the resource pack for better login times! <click:open_url:'https://github.com/HorizonsEndMC/ResourcePack'>https://github.com/HorizonsEndMC/ResourcePack</click>"
			)
		)
	}
}