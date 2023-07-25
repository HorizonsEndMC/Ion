package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.toBlockPos
import org.bukkit.entity.Player

@CommandPermission("ion.removeghostship")
@CommandAlias("removeghostship")
object RemoveGhostShipCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Default
	@Suppress("unused")
	fun onDeleteGhostShip(sender: Player) {
		val (x, y, z) = sender.location.toBlockPos()

		val ship = DeactivatedPlayerStarships.getLockedContaining(sender.world, x, y, z)

		ship?.let {
			PlayerStarshipData.remove(it._id)
			DeactivatedPlayerStarships.removeState(ship)
		}
	}
}
