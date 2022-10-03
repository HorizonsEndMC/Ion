package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.messageEmbed
import net.md_5.bungee.api.ProxyServer

@CommandAlias("playerlist")
@Description("List online players.")
class PlayerListCommand(private val proxy: ProxyServer) {
	@Default
	@Suppress("Unused")
	fun onPlayerListCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			messageEmbed(
				fields = proxy.serversCopy.values
					.filter { it.players.isNotEmpty() }
					.map { server ->
						val serverName = server.name.replaceFirstChar { it.uppercase() }

						MessageEmbed.Field(
							"$serverName *(${server.players.size} online)*",
							server.players.joinToString("\n", "", "") {
								it.name.replace("_", "\\_")
							},
							true
						)
					}
					.ifEmpty { null },
				description = if (proxy.onlineCount == 0) "*No players online*" else null
			)
		).setEphemeral(true).queue()
	}
}