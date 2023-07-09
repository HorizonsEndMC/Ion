package net.horizonsend.ion.server.miscellaneous.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.component3
import net.starlegacy.util.toBlockPos
import org.bukkit.entity.Player

@CommandPermission("ion.removeghostship")
@CommandAlias("removeghostship")
class RemoveGhostShipCommand : BaseCommand() {
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
