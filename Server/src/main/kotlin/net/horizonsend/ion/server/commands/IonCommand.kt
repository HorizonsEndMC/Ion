package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.IonServer
import org.bukkit.entity.Player

@CommandAlias("ion")
@CommandPermission("ion.command")
class IonCommand : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("configuration")
	inner class ConfigurationCommand : BaseCommand() {
		@Subcommand("reload")
		fun onIonConfigurationReloadCommand(player: Player) {
			IonServer.reloadConfiguration()
			player.sendFeedbackMessage(FeedbackType.SUCCESS, "Configuration Reloaded.")
		}
	}
}