package net.horizonsend.ion.proxy.commands.discord

import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.annotations.CommandMeta
import net.horizonsend.ion.common.annotations.Default

@CommandMeta("info", "List of useful links.")
class DiscordInfoCommand {
	@Default
	@Suppress("Unused")
	fun onInfoCommand(event: SlashCommandInteractionEvent) {
		event.replyEmbeds(
			MessageEmbed(null, "Here are a few links of potential use:",
				"[Survival Web Map](https://survival.horizonsend.net)\n" +
				"[Creative Web Map](https://creative.horizonsend.net)\n" +
				"[Discord Server](https://discord.gg/RPvgQsGzKM)\n" +
				"[Resource Pack](https://github.com/HorizonsEndMC/ResourcePack)\n" +
				"[Wiki](https://wiki.horizonsend.net)"
			, EmbedType.RICH, null, 0xff7f3f, null, null, null, null, null, null, null)
		).queue()
	}
}