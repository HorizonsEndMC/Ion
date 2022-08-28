package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import org.bukkit.entity.Player

@Suppress("Unused")
@CommandAlias("info")
class InfoCommand : BaseCommand() {
	@Default
	fun onInfoCommand(sender: Player) {
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Here are a few links of potential use:\n" +
			"<white><u><click:open_url:'https://survival.horizonsend.net'>Survival Web Map</click></u></white>\n" +
			"<white><u><click:open_url:'https://creative.horizonsend.net'>Creative Web Map</click></u></white>\n" +
			"<white><u><click:open_url:'https://discord.gg/RPvgQsGzKM'>Discord Server</click></u></white>\n" +
			"<white><u><click:open_url:'https://github.com/HorizonsEndMC/ResourcePack'>Resource Pack</click></u></white>\n" +
			"<white><u><click:open_url:'https://wiki.horizonsend.net'>Wiki</click></u></white>"
		)
	}
}