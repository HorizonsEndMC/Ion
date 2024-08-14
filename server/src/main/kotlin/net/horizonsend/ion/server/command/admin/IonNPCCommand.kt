package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.npcs.NPCManager
import org.bukkit.entity.Player
import java.util.function.Supplier

@CommandAlias("ionnpc")
object IonNPCCommand : SLCommand() {


	@Subcommand("create shipdealer")
	fun onCreateShipDealer(sender: Player) {

	}
}
