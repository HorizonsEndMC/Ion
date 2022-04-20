package net.horizonsend.ion.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.Ion
import org.bukkit.entity.Player

@CommandAlias("shrug")
internal class ShrugCommand(plugin: Ion): BaseCommand() {
	init { plugin.commandManager.registerCommand(this) }

	@Default
	fun onShrug(sender: Player) = sender.chat("¯\\_(ツ)_/¯")
}