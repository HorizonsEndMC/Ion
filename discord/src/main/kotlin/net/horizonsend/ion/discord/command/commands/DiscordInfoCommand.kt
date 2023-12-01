package net.horizonsend.ion.discord.command.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.discord.command.IonDiscordCommand
import net.horizonsend.ion.discord.command.annotations.CommandAlias
import net.horizonsend.ion.discord.command.annotations.Default
import net.horizonsend.ion.discord.command.annotations.Description
import net.horizonsend.ion.discord.utils.messageEmbed

@CommandAlias("info")
@Description("List of useful links.")
object DiscordInfoCommand : IonDiscordCommand() {
	@Default
	@Suppress("Unused")
	fun onInfoCommand(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
		event.replyEmbeds(
			messageEmbed(
				title = "Here are a few links of potential use:",
				description =
				"[Survival Web Map](https://survival.horizonsend.net)\n" +
					"[Creative Web Map](https://creative.horizonsend.net)\n" +
					"[Discord Server](https://discord.gg/RPvgQsGzKM)\n" +
					"[Resource Pack](https://github.com/HorizonsEndMC/ResourcePack/releases/latest)\n" +
					"[Wiki](https://wiki.horizonsend.net)\n" +
					"[Server Rules](https://wiki.horizonsend.net/rules)"
			)
		).queue()
	}
}
