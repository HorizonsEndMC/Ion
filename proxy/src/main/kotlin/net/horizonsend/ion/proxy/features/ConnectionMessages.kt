package net.horizonsend.ion.proxy.features

import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.messaging.ProxyDiscordMessaging
import net.horizonsend.ion.proxy.utils.isBanned
import net.horizonsend.ion.proxy.utils.sendRichMessage
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

object ConnectionMessages : IonProxyComponent() {
	fun onLogin(player: ProxiedPlayer, serverInfo: ServerInfo) { // This event is only called when logging into the server the first time
		if (player.isBanned()) return

		PLUGIN.proxy.sendRichMessage("<dark_gray>[<green>+ <gray>${serverInfo.name}<dark_gray>] <white>${player.name}")

		ProxyDiscordMessaging.globalEmbed(Embed(
			description = "[+ ${serverInfo.name}] ${player.name.replace("_", "\\_")}",
			color = NamedTextColor.GREEN.value()
		))
	}

	fun onSwitchServer(player: ProxiedPlayer, serverInfo: ServerInfo) {
		PLUGIN.proxy.sendRichMessage("<dark_gray>[<blue>> <gray>${serverInfo.name}<dark_gray>] <white>${player.name}")

		ProxyDiscordMessaging.globalEmbed(Embed(
			description = "[> ${serverInfo.name}] ${player.name.replace("_", "\\_")}",
			color = NamedTextColor.BLUE.value()
		))
	}

	fun onPlayerDisconnect(player: ProxiedPlayer, serverInfo: ServerInfo) {
		PLUGIN.proxy.sendRichMessage("<dark_gray>[<red>- <gray>$serverInfo<dark_gray>] <white>${player.displayName}")

		ProxyDiscordMessaging.globalEmbed(Embed(
			description = "[- ${serverInfo.name}] ${player.name.replace("_", "\\_")}",
			color = NamedTextColor.RED.value()
		))
	}
}
