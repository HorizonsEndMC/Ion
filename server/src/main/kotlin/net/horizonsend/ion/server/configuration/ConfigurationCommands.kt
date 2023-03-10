package net.horizonsend.ion.server.configuration

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.server.IonServer
import org.bukkit.command.CommandSender

@CommandAlias("ion")
@Suppress("unused")
class ConfigurationCommands : BaseCommand() {
	@Subcommand("config reload")
	@CommandPermission("ion.config.reload")
	fun onConfigReload(sender: CommandSender) {
		IonServer.configuration = Configuration.load(IonServer.dataFolder, "server.json")
		IonServer.balancing = Configuration.load(IonServer.dataFolder, "balancing.json")
		IonServer.soldShips = Configuration.load(IonServer.dataFolder, "soldShips.json")
		IonServer.shipList = listOf(IonServer.soldShips.soldShips.miner, IonServer.soldShips.soldShips.shuttle)
		sender.sendRichMessage("<bold><green>Configuration Reloaded")
	}
}
