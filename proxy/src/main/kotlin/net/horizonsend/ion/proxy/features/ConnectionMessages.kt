package net.horizonsend.ion.proxy.features

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.ServerInfo
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.discord.Discord
import net.horizonsend.ion.proxy.utils.ProxyMutes
import net.horizonsend.ion.proxy.utils.sendRichMessage
import net.kyori.adventure.text.format.NamedTextColor

object ConnectionMessages : IonProxyComponent() {
	private val global = PLUGIN.discordConfiguration.globalChannel

	fun onLogin(player: Player, serverInfo: ServerInfo) { // This event is only called when logging into the server the first time
		ProxyMutes.checkBan(player.uniqueId).whenComplete { banned, exception ->
			if (exception != null) throw exception

			if (banned) return@whenComplete

			PLUGIN.proxy.sendRichMessage("<dark_gray>[<green>+ <gray>${serverInfo.name}<dark_gray>] <white>${player.username}")

			Discord.sendEmbed(global, Embed(
				description = "[+ ${serverInfo.name}] ${player.username.replace("_", "\\_")}",
				color = NamedTextColor.GREEN.value()
			))
		}
	}

	fun onSwitchServer(player: Player, serverInfo: ServerInfo) {
		PLUGIN.proxy.sendRichMessage("<dark_gray>[<blue>> <gray>${serverInfo.name}<dark_gray>] <white>${player.username}")

		Discord.sendEmbed(global, Embed(
			description = "[> ${serverInfo.name}] ${player.username.replace("_", "\\_")}",
			color = NamedTextColor.BLUE.value()
		))
	}

	fun onPlayerDisconnect(player: Player, serverInfo: ServerInfo) {
		PLUGIN.proxy.sendRichMessage("<dark_gray>[<red>- <gray>${serverInfo.name}<dark_gray>] <white>${player.username}")

		Discord.sendEmbed(global, Embed(
			description = "[- ${serverInfo.name}] ${player.username.replace("_", "\\_")}",
			color = NamedTextColor.RED.value()
		))
	}
}
