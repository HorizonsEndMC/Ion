package net.horizonsend.ion.proxy.commands.discord

import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.discord.DiscordCommand
import net.horizonsend.ion.proxy.features.discord.DiscordSubcommand
import net.horizonsend.ion.proxy.features.discord.SlashCommandManager
import net.horizonsend.ion.proxy.features.misc.ServerMessaging
import net.horizonsend.ion.proxy.utils.messageEmbed

object DiscordTPSCommand : DiscordCommand("tps", "Get server TPS") {
	val proxy: ProxyServer = PLUGIN.server

	override fun setup(commandManager: SlashCommandManager) {
		registerDefaultReceiver(defaultReceiver)
	}

	private val defaultReceiver = object : DiscordSubcommand("tps", "Get server TPS", listOf()) {
		override fun execute(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
			event.replyEmbeds(
				messageEmbed(
				title = "Server TPS",
				fields = proxy.allServers
					.filter { it.playersConnected.isNotEmpty() }
					.map { server ->
						val serverName = server.serverInfo.name.replaceFirstChar { it.uppercase() }
						val tps = ServerMessaging.getTps(server)

						MessageEmbed.Field(
							serverName,
							tps.toString(),
							true
						)
					}
					.ifEmpty { null }
			)
			).setEphemeral(true).queue()
		}
	}
}
