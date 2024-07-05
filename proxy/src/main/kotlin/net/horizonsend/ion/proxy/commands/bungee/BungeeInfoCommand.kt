package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent.openUrl
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor.fromHexString
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED

@CommandAlias("info|map|wiki|patreon|rules")
class BungeeInfoCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onInfoCommand(sender: Player) {
		val text = text()
			.append(text("Here are a few links of potential use:\n", fromHexString("#8888ff")))
			.append(text("Survival Web Map\n", WHITE, UNDERLINED).clickEvent(openUrl("https://survival.horizonsend.net")))
			.append(text("Creative Web Map\n", WHITE, UNDERLINED).clickEvent(openUrl("https://creative.horizonsend.net")))
			.append(text("Discord Server\n", WHITE, UNDERLINED).clickEvent(openUrl("https://discord.gg/RPvgQsGzKM")))
			.append(text("Resource Pack\n", WHITE, UNDERLINED).clickEvent(openUrl("https://github.com/HorizonsEndMC/ResourcePack/releases/latest")))
			.append(text("Wiki\n", WHITE, UNDERLINED).clickEvent(openUrl("https://wiki.horizonsend.net")))
			.append(text("Patreon\n", WHITE, UNDERLINED).clickEvent(openUrl("https://www.patreon.com/horizonsendmc")))
			.append(text("Server Rules\n", WHITE, UNDERLINED).clickEvent(openUrl("Server Rules")))

		sender.sendMessage(text.build())
	}
}
