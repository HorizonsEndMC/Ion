package net.horizonsend.ion.proxy.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.horizonsend.ion.common.Colors
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.special
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.discord
import net.horizonsend.ion.proxy.utils.messageEmbed
import kotlin.jvm.optionals.getOrNull

class PlayerListeners {
	@Subscribe
	fun join(e: ServerConnectedEvent) {
		if (!e.previousServer.isPresent) {
			IonProxy.proxy.information("<dark_gray>[<green>+ <gray>${e.server.serverInfo.name}<dark_gray>] <white>${e.player.username}")

			discord?.getTextChannelById(IonProxy.configuration.globalChannel)?.sendMessageEmbeds(
				messageEmbed(
					description = "[+ ${e.server.serverInfo.name}] ${e.player.username.replace("_", "\\_")}",
					color = Colors.SUCCESS
				)
			)?.queue()

			e.player.special("Hey ${e.player.username}! Remember to vote for the server to help us grow the Horizon's End community!")
		} else {
			IonProxy.proxy.information("<dark_gray>[<blue>> <gray>${e.server.serverInfo.name}<dark_gray>] <white>${e.player.username}")

			discord?.getTextChannelById(IonProxy.configuration.globalChannel)?.sendMessageEmbeds(
				messageEmbed(
					description = "[> ${e.server.serverInfo.name}] ${e.player.username.replace("_", "\\_")}",
					color = Colors.INFORMATION
				)
			)?.queue()
		}
	}

	@Subscribe
	fun leave(e: DisconnectEvent) {
		if (e.loginStatus != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return

		val serverName = e.player.currentServer.getOrNull()?.server?.serverInfo?.name

		IonProxy.proxy.information(
			"<dark_gray>[<red>- " +
				"<gray>$serverName<dark_gray>] " +
				"<white>${e.player.username}"
		)


		discord?.getTextChannelById(IonProxy.configuration.globalChannel)?.sendMessageEmbeds(
			messageEmbed(
				description = "[- $serverName] ${e.player.username.replace("_", "\\_")}",
				color = Colors.USER_ERROR
			)
		)?.queue()
	}
}
