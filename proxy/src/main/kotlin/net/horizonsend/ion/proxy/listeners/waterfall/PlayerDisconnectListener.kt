package net.horizonsend.ion.proxy.listeners.waterfall

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class PlayerDisconnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
		val serverName = PLUGIN.playerServerMap.remove(event.player)!!.name

		PLUGIN.proxy.information("<dark_gray>[<red>- <gray>$serverName<dark-gray>] <white>${event.player.displayName}")

		PLUGIN.discord?.let { jda ->
			val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

			globalChannel.sendMessageEmbeds(
				messageEmbed(
					description = "[- $serverName] ${event.player.name.replace("_", "\\_")}",
					color = ChatColor.RED.color.rgb
				)
			).queue()
		}
	}
}
