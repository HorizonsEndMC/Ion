package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.Default
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.annotations.CommandMeta
import net.horizonsend.ion.proxy.proxy
import net.horizonsend.ion.proxy.utilities.messageEmbed

@CommandMeta("playerlist", "List online players.")
class PlayerListCommand {
	@Default
	@Suppress("Unused")
	fun onPlayerListCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			messageEmbed(fields = proxy.allServers
				.filter { it.playersConnected.isNotEmpty() }
				.map { server ->
					MessageEmbed.Field(
						"${server.serverInfo.name.replaceFirstChar { it.uppercase() }} *(${server.playersConnected.size} online)*",
						server.playersConnected.joinToString("\n", "", "") { it.username.replace("_", "\\_") },
						true
					)
				}
				.ifEmpty { listOf(MessageEmbed.Field(null, "*No players online*", true)) }
			)
		).setEphemeral(true).queue()
	}
}