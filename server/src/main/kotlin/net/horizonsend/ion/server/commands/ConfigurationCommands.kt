package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

@CommandAlias("serverConfiguration")
@Suppress("unused")
class ConfigurationCommands : BaseCommand() {
	@Subcommand("reload")
	@CommandPermission("ion.config.reload")
	fun onConfigReload(sender: CommandSender){
		IonServer.Ion.configuration = loadConfiguration(IonServer.Ion.dataFolder, "server.conf")
		IonServer.Ion.balancing = loadConfiguration(IonServer.Ion.dataFolder, "balancing.conf")
		sender.sendMessage(MiniMessage.miniMessage().deserialize("<bold><green>Configuration Reloaded"))
	}
}