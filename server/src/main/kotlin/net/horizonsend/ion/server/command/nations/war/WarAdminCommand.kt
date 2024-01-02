package net.horizonsend.ion.server.command.nations.war

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.server.command.SLCommand

@CommandAlias("waradmin")
@CommandPermission("ion.war.admin")
object WarAdminCommand : SLCommand() {
	fun onEnd(): Nothing = TODO()
	fun onAdd(): Nothing = TODO()
	fun onRename(): Nothing = TODO()
}
