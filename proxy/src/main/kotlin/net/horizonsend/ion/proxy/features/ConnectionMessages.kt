package net.horizonsend.ion.proxy.features

import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.utils.isBanned
import net.horizonsend.ion.proxy.utils.messageEmbed
import net.horizonsend.ion.proxy.utils.sendRichMessage
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

object ConnectionMessages : IonProxyComponent() {
	fun onLogin(player: ProxiedPlayer, serverInfo: ServerInfo) { // This event is only called when logging into the server the first time
		if (player.isBanned()) return

		PLUGIN.proxy.sendRichMessage("<dark_gray>[<green>+ <gray>${serverInfo.name}<dark_gray>] <white>${player.name}")

		PLUGIN.discord?.let { jda ->
			val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

			globalChannel.sendMessageEmbeds(
				messageEmbed(
					description = "[+ ${serverInfo.name}] ${player.name.replace("_", "\\_")}",
					color = ChatColor.GREEN.color.rgb
				)
			).queue()
		}
	}

	fun onSwitchServer(player: ProxiedPlayer, serverInfo: ServerInfo) {
		PLUGIN.proxy.sendRichMessage("<dark_gray>[<blue>> <gray>${serverInfo.name}<dark_gray>] <white>${player.name}")

		PLUGIN.discord?.let { jda ->
			val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

			globalChannel.sendMessageEmbeds(
				messageEmbed(
					description = "[> ${serverInfo.name}] ${player.name.replace("_", "\\_")}",
					color = ChatColor.BLUE.color.rgb
				)
			).queue()
		}
	}

	fun onPlayerDisconnect(player: ProxiedPlayer, serverInfo: ServerInfo) {
		PLUGIN.proxy.sendRichMessage("<dark_gray>[<red>- <gray>${serverInfo.name}<dark_gray>] <white>${player.displayName}")

		PLUGIN.discord?.let { jda ->
			val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

			globalChannel.sendMessageEmbeds(
				messageEmbed(
					description = "[- ${serverInfo.name}] ${player.name.replace("_", "\\_")}",
					color = ChatColor.RED.color.rgb
				)
			).queue()
		}
	}
}
