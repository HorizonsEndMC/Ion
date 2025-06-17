package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.npcs.database.DatabaseNPCs
import net.horizonsend.ion.server.features.npcs.database.metadata.ServerShipDealerMetadata
import net.horizonsend.ion.server.features.npcs.database.type.DatabaseNPCTypes
import net.horizonsend.ion.server.miscellaneous.utils.Skins
import org.bukkit.entity.Player

@CommandPermission("ion.npc.command")
@CommandAlias("ionnpc")
object IonNPCCommand : SLCommand() {

	@Subcommand("create shipdealer")
	fun onCreateShipDealer(sender: Player) = asyncCommand(sender) {
		DatabaseNPCs.spawn(sender, sender.location, DatabaseNPCTypes.SERVER_SHIP_DEALER, ServerShipDealerMetadata(), Skins[sender.uniqueId]!!)
	}


}
