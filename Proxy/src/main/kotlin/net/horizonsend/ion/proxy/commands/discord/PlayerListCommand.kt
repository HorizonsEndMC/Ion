package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.annotations.GuildCommand
import net.horizonsend.ion.proxy.messageEmbed

@GuildCommand
@Suppress("Unused")
@CommandAlias("playerlist")
@Description("List online players.")
class PlayerListCommand(private val velocity: ProxyServer) {
	@Default
	fun onPlayerListCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			messageEmbed(
				fields = velocity.allServers
					.filter { it.playersConnected.isNotEmpty() }
					.map { server ->
						val serverName = server.serverInfo.name.replaceFirstChar { it.uppercase() }

						MessageEmbed.Field(
							"$serverName *(${server.playersConnected.size} online)*",
							server.playersConnected.joinToString("\n", "", "") {
								it.username.replace("_", "\\_")
							},
							true
						)
					}
					.ifEmpty { null },
				description = if (velocity.playerCount == 0) "*No players online*" else null
			)
		).setEphemeral(true).queue()
	}
}