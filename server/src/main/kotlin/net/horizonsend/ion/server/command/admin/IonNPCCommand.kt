package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player

@CommandAlias("ionnpc")
object IonNPCCommand : SLCommand() {
	//TODO

	@Subcommand("create shipdealer")
	fun onCreateShipDealer(sender: Player) {

	}
}
