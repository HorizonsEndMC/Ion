package net.horizonsend.ion.discord.command.commands

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.utils.Server
//import net.horizonsend.ion.common.utils.Servers
import net.horizonsend.ion.discord.command.IonDiscordCommand
import net.horizonsend.ion.discord.command.annotations.CommandAlias
import net.horizonsend.ion.discord.command.annotations.Default
import net.horizonsend.ion.discord.command.annotations.Description
import net.horizonsend.ion.discord.features.PlayerTracking.getAllPlayers
import net.horizonsend.ion.discord.features.PlayerTracking.getPlayers
//import net.horizonsend.ion.discord.features.redis.Messaging.getPlayers
import net.horizonsend.ion.discord.utils.messageEmbed

@CommandAlias("playerlist")
@Description("List online players.")
object PlayerListCommand : IonDiscordCommand() {
	val individual = listOf(Server.SURVIVAL, Server.CREATIVE)

	@Default
	@Suppress("Unused")
	fun onPlayerListCommand(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
		val playerCount = getAllPlayers().count()

		event.replyEmbeds(
			messageEmbed(
				fields = individual.map { server ->
					val players = getPlayers(server)

					MessageEmbed.Field(
						"${server.displayName} *(${players.size} online)*",
						players.joinToString("\n", "", "") { it.name.replace("_", "\\_") },
						true
					)
				}.ifEmpty { null },
				description = if (playerCount == 0) "*No players online*" else "$playerCount total players."
			)
		).setEphemeral(true).queue()
	}
}
