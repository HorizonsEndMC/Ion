package net.horizonsend.ion.server.command.space

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.space.SpaceWorlds
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("spaceworld")
@CommandPermission("space.spaceworld")
object SpaceWorldCommand : SLCommand() {
	@Subcommand("list")
	fun onList(sender: CommandSender) {
		sender.information(
			"Space Worlds: " + (
				Bukkit.getWorlds()
					.filter { SpaceWorlds.contains(it) }
					.takeIf { it.isNotEmpty() }
					?.joinToString { it.name }
					?: "None"
				)
		)
	}
}
