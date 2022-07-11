package net.horizonsend.ion.proxy.commands.discord

import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.annotations.CommandMeta
import net.horizonsend.ion.common.annotations.Default

@CommandMeta("playerlist", "List online players.")
class PlayerListCommand(private val proxy: ProxyServer) {
	@Default
	@Suppress("Unused")
	fun onPlayerListCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			MessageEmbed(null, null, null, EmbedType.RICH, null, 0xff7f3f, null, null, null, null, null, null,
				proxy.allServers.map { server ->
					MessageEmbed.Field(
						"${server.serverInfo.name.replaceFirstChar { it.uppercase() }} *(${server.playersConnected.size} online)*",
						server.playersConnected.joinToString("\n", "", "") { it.username }.ifBlank { "*No players online*" },
						true
					)
				}
			)
		).setEphemeral(true).queue()
	}
}