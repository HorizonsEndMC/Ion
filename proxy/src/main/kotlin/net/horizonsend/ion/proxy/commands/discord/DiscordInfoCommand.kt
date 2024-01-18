package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.proxy.utils.messageEmbed

@CommandAlias("info")
@Description("List of useful links.")
object DiscordInfoCommand : IonDiscordCommand() {
	@Default
	@Suppress("Unused")
	fun onInfoCommand(event: SlashCommandInteractionEvent) = asyncDiscordCommand(event) {
		event.replyEmbeds(
			messageEmbed(
				title = "Here are a few links of potential use:",
				description = """
					[Survival Web Map](https://survival.horizonsend.net)
					[Creative Web Map](https://creative.horizonsend.net)
					[Discord ServerType](https://discord.gg/RPvgQsGzKM)
					[Resource Pack](https://github.com/HorizonsEndMC/ResourcePack/releases/latest)
					[Wiki](https://wiki.horizonsend.net)
					[ServerType Rules](https://wiki.horizonsend.net/rules)
				""".trimIndent()
			)
		).queue()
	}
}
