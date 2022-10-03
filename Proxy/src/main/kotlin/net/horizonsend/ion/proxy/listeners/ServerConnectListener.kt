package net.horizonsend.ion.proxy.listeners

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.messageEmbed
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
			val playerData = PlayerData[event.player.uniqueId]

			if (playerData.minecraftUsername == null) {
				IonProxy.proxy.broadcast(
					*ComponentBuilder()
						.append(ComponentBuilder("Welcome ").color(ChatColor.GOLD).create())
						.append(ComponentBuilder(event.player.name).color(ChatColor.WHITE).create())
						.append(ComponentBuilder(" to the server!").color(ChatColor.GOLD).create())
						.create()
				)

				IonProxy.jda?.let { jda ->
					val globalChannel = jda.getTextChannelById(IonProxy.configuration.globalChannel) ?: return@let

					globalChannel.sendMessageEmbeds(
						messageEmbed(
							title = "Welcome ${event.player.name} to the server!",
							color = ChatColor.GOLD.color.rgb
						)
					).queue()
				}
			} else {
				IonProxy.proxy.broadcast(
					*ComponentBuilder()
						.append(ComponentBuilder("[").color(ChatColor.DARK_GRAY).create())
						.append(ComponentBuilder("+ ").color(ChatColor.GREEN).create())
						.append(ComponentBuilder(event.target.name).color(ChatColor.GRAY).create())
						.append(ComponentBuilder("] ").color(ChatColor.DARK_GRAY).create())
						.append(ComponentBuilder(event.player.displayName).color(ChatColor.WHITE).create())
						.create()
				)

				IonProxy.jda?.let { jda ->
					val globalChannel = jda.getTextChannelById(IonProxy.configuration.globalChannel) ?: return@let

					globalChannel.sendMessageEmbeds(
						messageEmbed(
							title = "[+ ${event.target.name}] ${event.player.name}",
							color = ChatColor.GREEN.color.rgb
						)
					).queue()
				}
			}

			if (playerData.minecraftUsername != event.player.name) {
				playerData.update { minecraftUsername = event.player.name }
			}

			IonProxy.jda?.let { jda ->
				val discordId = playerData.discordId ?: return

				val guild = jda.getGuildById(IonProxy.configuration.discordServer) ?: return

				guild.addRoleToMember(
					guild.getMemberById(discordId) ?: return,
					guild.getRoleById(IonProxy.configuration.onlineRole) ?: return
				).queue()
			}
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

			IonProxy.jda?.let { jda ->
				val globalChannel = jda.getTextChannelById(IonProxy.configuration.globalChannel) ?: return@let

				globalChannel.sendMessageEmbeds(
					messageEmbed(
						title = "[> ${event.target.name}] ${event.player.name}",
						color = ChatColor.BLUE.color.rgb
					)
				).queue()
			}
		}
	}
}