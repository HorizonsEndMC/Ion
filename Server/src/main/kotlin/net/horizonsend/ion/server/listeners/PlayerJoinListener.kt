package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.common.configuration.ConfigurationProvider
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("Unused")
	fun onPlayerJoinEvent(event: PlayerJoinEvent) {
		event.joinMessage(null)

		event.player.sendPlayerListHeader(miniMessage().deserialize(
			"\n<blue>Horizon's End</blue>\n\n" +
			ConfigurationProvider.sharedConfiguration.tablistHeaderMessage.run { if (isEmpty()) "" else "$this\n\n" }
		))
	}
}