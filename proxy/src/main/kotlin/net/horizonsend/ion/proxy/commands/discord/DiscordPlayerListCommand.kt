package net.horizonsend.ion.proxy.commands.discord

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.discord.DiscordCommand
import net.horizonsend.ion.proxy.features.discord.DiscordSubcommand
import net.horizonsend.ion.proxy.features.discord.SlashCommandManager
import net.horizonsend.ion.proxy.messageEmbed
import net.md_5.bungee.api.ProxyServer

object DiscordPlayerListCommand : DiscordCommand("playerlist", "List all players") {
	val proxy: ProxyServer = PLUGIN.getProxy()

	override fun setup(commandManager: SlashCommandManager) {
		registerDefaultReceiver(defaultReceiver)
	}

	val defaultReceiver = object : DiscordSubcommand("list", "list all players", listOf()) {
		override fun execute(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
			event.replyEmbeds(messageEmbed(
				title = "Horizon's End Players",
				fields = proxy.serversCopy.values
					.filter { it.players.isNotEmpty() }
					.map { server ->
						val serverName = server.name.replaceFirstChar { it.uppercase() }

						MessageEmbed.Field(
							"$serverName *(${server.players.size} online)*",
							server.players.joinToString("\n", "", "") {
								it.name.replace("_", "\\_") },
							true
						)
					}
					.ifEmpty { null },
				description = if (proxy.onlineCount == 0) "*No players online*" else "${proxy.onlineCount} total players."
			)).setEphemeral(true).queue()
		}
	}
}
