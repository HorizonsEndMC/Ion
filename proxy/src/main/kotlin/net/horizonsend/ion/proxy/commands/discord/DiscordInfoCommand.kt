package net.horizonsend.ion.proxy.commands.discord

import net.horizonsend.ion.proxy.features.discord.DiscordCommand
import net.horizonsend.ion.proxy.features.discord.DiscordSubcommand.Companion.subcommand
import net.horizonsend.ion.proxy.features.discord.SlashCommandManager
import net.horizonsend.ion.proxy.utils.messageEmbed

object DiscordInfoCommand : DiscordCommand("info", "Get a list of useful links") {
	override fun setup(commandManager: SlashCommandManager) {
		registerDefaultReceiver(default)
	}

	val default = subcommand(name, description, listOf()) { event ->
		event.replyEmbeds(
			messageEmbed(
				title = "Here are a few links of potential use:",
				description = """
					[Survival Web Map](https://survival.horizonsend.net)
					[Creative Web Map](https://creative.horizonsend.net)
					[Discord Server](https://discord.gg/RPvgQsGzKM)
					[Resource Pack](https://github.com/HorizonsEndMC/ResourcePack/releases/latest)
					[Wiki](https://wiki.horizonsend.net)
					[Server Rules](https://wiki.horizonsend.net/rules)
				""".trimIndent()
			)
		).queue()
	}
}
