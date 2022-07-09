package net.horizonsend.ion.proxy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage

@CommandAlias("info")
class InfoCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onLinksCommand(sender: Player) {
		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Here are a few links of potential use:\n" +
			"<white><u><click:open_url:'https://survival.horizonsend.net'>Survival Web Map</click></u></white>" +
			"<white><u><click:open_url:'https://creative.horizonsend.net'>Creative Web Map</click></u></white>" +
			"<white><u><click:open_url:'https://discord.gg/RPvgQsGzKM'>Discord Server</click></u></white>" +
			"<white><u><click:open_url:'https://wiki.horizonsend.net'>Wiki</click></u></white>" +
			"<white><u><click:open_url:'https://github.com/HorizonsEndMC/ResourcePack'>Resource Pack</click></u></white>"
		)
	}
}