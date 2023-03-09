package net.horizonsend.ion.proxy.listeners.waterfall

import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class PlayerDisconnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
		val serverName = PLUGIN.playerServerMap.remove(event.player)!!.name

		PLUGIN.proxy.broadcast(
			*ComponentBuilder()
				.append(ComponentBuilder("[").color(ChatColor.DARK_GRAY).create())
				.append(ComponentBuilder("- ").color(ChatColor.RED).create())
				.append(ComponentBuilder(serverName).color(ChatColor.GRAY).create())
				.append(ComponentBuilder("] ").color(ChatColor.DARK_GRAY).create())
				.append(ComponentBuilder(event.player.displayName).color(ChatColor.WHITE).create())
				.create()
		)

		PLUGIN.jda?.let { jda ->
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
