package net.horizonsend.ion.miscellaneous

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import org.bukkit.entity.Player

@CommandAlias("shrug")
internal class ShrugCommand: BaseCommand() {
	@Default
	@Suppress("unused") // Command
	fun onShrug(sender: Player) = sender.chat("¯\\_(ツ)_/¯")
}