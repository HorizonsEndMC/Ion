package net.horizonsend.ion.proxy.listeners

import net.horizonsend.ion.proxy.IonProxy
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class ServerConnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerConnectEvent(event: ServerConnectEvent) {
		if (event.reason == ServerConnectEvent.Reason.JOIN_PROXY) {
			IonProxy.proxy.broadcast(
				*ComponentBuilder()
					.append(ComponentBuilder("[").color(ChatColor.DARK_GRAY).create())
					.append(ComponentBuilder("+ ").color(ChatColor.GREEN).create())
					.append(ComponentBuilder(event.target.name).color(ChatColor.GRAY).create())
					.append(ComponentBuilder("] ").color(ChatColor.DARK_GRAY).create())
					.append(ComponentBuilder(event.player.displayName).color(ChatColor.WHITE).create())
					.create()
			)
		} else {
			IonProxy.proxy.broadcast(
				*ComponentBuilder()
					.append(ComponentBuilder("[").color(ChatColor.DARK_GRAY).create())
					.append(ComponentBuilder("> ").color(ChatColor.BLUE).create())
					.append(ComponentBuilder(event.target.name).color(ChatColor.GRAY).create())
					.append(ComponentBuilder("] ").color(ChatColor.DARK_GRAY).create())
					.append(ComponentBuilder(event.player.displayName).color(ChatColor.WHITE).create())
					.create()
			)
		}
	}
}