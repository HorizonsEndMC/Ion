package net.horizonsend.ion.server.command.nations.war

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player

@CommandAlias("waradmin")
@CommandPermission("ion.war.admin")
object WarAdminCommand : SLCommand() {
	fun onEnd(sender: Player) = asyncCommand(sender) {

	}

	fun onAdd(sender: Player) = asyncCommand(sender) {

	}

	fun onRename(sender: Player) = asyncCommand(sender) {

	}
}
