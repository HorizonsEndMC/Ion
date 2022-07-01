package net.horizonsend.ion.proxy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage

@CommandAlias("links")
class LinksCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onLinksCommand(sender: Player) {
		sender.sendFeedbackMessage(FeedbackType.INFORMATION,
			"""
				Survival Web Map: <white><u><click:open_url:'https://survival.horizonsend.net'>survival.horizonsend.net</click></u></white>
				Creative Web Map: <white><u><click:open_url:'https://survival.horizonsend.net'>creative.horizonsend.net</click></u></white>
				Discord Server: <white><u><click:open_url:'https://discord.gg/RPvgQsGzKM'>discord.gg/RPvgQsGzKM</click></u></white>
				Wiki: <white><u><click:open_url:'https://wiki.horizonsend.net'>wiki.horizonsend.net</click></u></white>
			""".trimIndent()
		)
	}
}