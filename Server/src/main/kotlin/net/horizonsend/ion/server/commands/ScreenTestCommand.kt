package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.managers.ScreenManager.openScreen
import net.horizonsend.ion.server.screens.TextScreen
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.entity.Player

@CommandAlias("screentest")
class ScreenTestCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onCommand(sender: Player, screenMiniMessage: String) {
		sender.openScreen(TextScreen(miniMessage().deserialize(screenMiniMessage.replace("\\n", "\n")) as TextComponent))
	}
}