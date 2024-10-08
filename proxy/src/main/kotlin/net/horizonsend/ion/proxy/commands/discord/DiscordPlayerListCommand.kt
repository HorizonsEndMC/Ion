package net.horizonsend.ion.proxy.commands.discord

import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.discord.DiscordCommand
import net.horizonsend.ion.proxy.features.discord.DiscordSubcommand
import net.horizonsend.ion.proxy.features.discord.SlashCommandManager
import net.horizonsend.ion.proxy.utils.messageEmbed

object DiscordPlayerListCommand : DiscordCommand("playerlist", "List all players") {
	val proxy: ProxyServer = PLUGIN.server

	override fun setup(commandManager: SlashCommandManager) {
		registerDefaultReceiver(defaultReceiver)
	}

	val defaultReceiver = object : DiscordSubcommand("list", "list all players", listOf()) {
		override fun execute(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
			event.replyEmbeds(messageEmbed(
				title = "Horizon's End Players",
				fields = proxy.allServers
					.filter { it.playersConnected.isNotEmpty() }
					.map { server ->
						val serverName = server.serverInfo.name.replaceFirstChar { it.uppercase() }

						MessageEmbed.Field(
							"$serverName *(${server.playersConnected.size} online)*",
							server.playersConnected.joinToString("\n", "", "") {
								it.username.replace("_", "\\_") },
							true
						)
					}
					.ifEmpty { null },
				description = if (proxy.playerCount == 0) "*No players online*" else "${proxy.playerCount} total players."
			)).setEphemeral(true).queue()
		}
	}
}
