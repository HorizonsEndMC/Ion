package net.horizonsend.ion.proxy.commands.discord

import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.annotations.CommandMeta
import net.horizonsend.ion.common.annotations.Default
import net.horizonsend.ion.proxy.utilities.messageEmbed

@CommandMeta("playerlist", "List online players.")
class PlayerListCommand(private val proxy: ProxyServer) {
	@Default
	@Suppress("Unused")
	fun onPlayerListCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			messageEmbed(fields = proxy.allServers
				.filter { it.playersConnected.isNotEmpty() }
				.map { server -> MessageEmbed.Field(
						"${server.serverInfo.name.replaceFirstChar { it.uppercase() }} *(${server.playersConnected.size} online)*",
						server.playersConnected.joinToString("\n", "", "") { it.username },
						true
				)}
				.ifEmpty { listOf(MessageEmbed.Field(null, "*No players online*", true)) }
			)
		).setEphemeral(true).queue()
	}
}